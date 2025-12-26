import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@WebServlet("/ChatServlet")
public class ChatServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String userMsg = req.getParameter("message");
        HttpSession session = req.getSession();

        String neededInfo = (String) session.getAttribute("neededInfo");
        String pendingIntent = (String) session.getAttribute("pendingIntent");

        String reply;

        // FIRST check follow-up
        if (neededInfo != null && pendingIntent != null) {
            reply = handleFollowUp(userMsg, session);
        } else {
            // Normal Rasa intent
            RasaClient.RasaResult rasa = RasaClient.interpret(userMsg);
            reply = handleIntent(rasa, session);
        }

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(reply);
    }

    /* ==========================================================
                        INTENT HANDLER
       ========================================================== */
    private String handleIntent(RasaClient.RasaResult rasa, HttpSession session) {

        switch (rasa.intent) {
            case "exam_schedule":
                return processExam(rasa, session);

            case "fees_query":
                return processFees(rasa, session);

            case "faculty_query":
                return processFaculty(rasa, session);

            case "event_query":
                return processEvent(rasa, session);

            case "greet":
                return "Hello! How can I help you today?";

            case "goodbye":
                return "Thank you! Feel free to ask anything anytime.";
                
            case "affirm":
            	return "Is that helpful?";

            default:
                return "I'm not sure I understood. Can you rephrase your question?";
        }
    }

	    /* ==========================================================
	    FOLLOW-UP HANDLING
	========================================================== */
	private String handleFollowUp(String userMsg, HttpSession session) {
	
	String need = (String) session.getAttribute("neededInfo");
	String pending = (String) session.getAttribute("pendingIntent");
	
	RasaClient.RasaResult temp = new RasaClient.RasaResult();
	temp.intent = pending;
	temp.text = userMsg;
	
	// Initialize the entities map
	temp.entities = new HashMap<>();
	
	switch (need) {
	
	case "semester":
	int sem = parseInteger(userMsg);
	if (sem < 1 || sem > 6) {
	return "Please provide a valid semester number (1–6).";
	}
	
	temp.entities.put("semester", String.valueOf(sem));
	
	session.removeAttribute("neededInfo");
	session.removeAttribute("pendingIntent");
	
	if (pending.equals("exam_schedule")) return processExam(temp, session);
	if (pending.equals("fees_query")) return processFees(temp, session);
	
	break;
	
	case "facultyName":
	String name = userMsg.trim();
	if (name.isEmpty()) return "Please provide a valid faculty name.";
	
	temp.entities.put("facultyName", name);
	
	session.removeAttribute("neededInfo");
	session.removeAttribute("pendingIntent");
	
	return processFaculty(temp, session);
	
	case "year":
	int year = parseInteger(userMsg);
	if (year < 1900 || year > 2100) {
	return "Please provide a valid event year (e.g., 2025).";
	}
	
	temp.entities.put("year", String.valueOf(year));
	
	session.removeAttribute("neededInfo");
	session.removeAttribute("pendingIntent");
	
	return processEvent(temp, session);
	}
	
	return "I still need more details. Please try again.";
	}

    /* ==========================================================
                          EXAM HANDLER
       ========================================================== */
    private String processExam(RasaClient.RasaResult rasa, HttpSession session) {

        int sem = extractSemester(rasa);
        if (sem == -1) {
            session.setAttribute("neededInfo", "semester");
            session.setAttribute("pendingIntent", "exam_schedule");
            return "To check the exam schedule, please provide your semester number (1–6).";
        }

        List<DButil.Exam> list = DButil.searchExams("ANY", "ANY", "ANY", sem);

        if (list == null || list.isEmpty()) {
            return "No exam schedule found for semester " + sem + ".";
        }

        StringBuilder sb = new StringBuilder("Exam Schedule");

        for (DButil.Exam e : list) {
            sb.append("<br><br>Course: ").append(e.courseName)
              .append("<br>Course Code: ").append(e.courseCode)
              .append("<br>Type: ").append(e.examType)
              .append("<br>Date: ").append(e.examDate)
              .append("<br>Class: ").append(e.classes)
              .append("<br>Department: ").append(e.deptName)
              .append("<br><br>"); // blank line
        }



        return sb.toString();
    }

    /* Extract semester safely */
    private int extractSemester(RasaClient.RasaResult rasa) {

        try {
            // Check if semester entity exists
            if (rasa.entities.containsKey("semester")) {
                return Integer.parseInt(rasa.entities.get("semester"));
            }

            // If no entity extracted, try to pull a number from the user message
            return parseInteger(rasa.text);

        } catch (Exception e) {
            return -1;
        }
    }

    
    /*Extract extractYear*/
    private int extractYear(RasaClient.RasaResult rasa) {
        try {
            if (rasa.entities.containsKey("year")) {
                int y = Integer.parseInt(rasa.entities.get("year"));
                if (String.valueOf(y).length() == 4) return y;
            }
            if (rasa.entities.containsKey("years")) {
                int y = Integer.parseInt(rasa.entities.get("years"));
                if (String.valueOf(y).length() == 4) return y;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }



    /* ==========================================================
                          FEES HANDLER
       ========================================================== */
    private String processFees(RasaClient.RasaResult rasa, HttpSession session) {

        // 1. Extract semester
        int sem = extractSemester(rasa);
        if (sem == -1) {
            session.setAttribute("neededInfo", "semester");
            session.setAttribute("pendingIntent", "fees_query");
            return "For fee details, please provide the semester number (1–6).";
        }

        // 2. Extract year (if user mentions it)
        int year = extractYear(rasa);

        // If year is NOT mentioned → use current year
        if (year == -1) {
        	year = LocalDate.now().getYear();
        }

        // 3. Search fees in database
        List<DButil.Fee> list = DButil.searchFees(year, sem);

        // 4. No record found
        if (list == null || list.isEmpty()) {
            return "No fee details found for Semester " + sem + ".";
        }

        // 5. Return fee details
        DButil.Fee f = list.get(0);

        return "<b>Fee Details</b>"
                + "<br>Year: " + year
                + "<br>Semester: " + f.semester
                + "<br>Amount: ₹" + f.amount
                + "<br>Last Date: " + f.lastDate;
    }


	    /* ==========================================================
	    FACULTY HANDLER
	========================================================== */
	private String processFaculty(RasaClient.RasaResult rasa, HttpSession session) {
	
	// Check if "facultyName" entity exists, otherwise fallback to text
	String name = (rasa.entities.containsKey("facultyName")) 
	   ? rasa.entities.get("facultyName") 
	   : rasa.text;
	
	if (name == null || name.trim().isEmpty()) {
	session.setAttribute("neededInfo", "facultyName");
	session.setAttribute("pendingIntent", "faculty_query");
	return "Please provide the faculty name.";
	}
	
	List<DButil.Faculty> list = DButil.getFaculty(name.trim());
	
	if (list == null || list.isEmpty()) {
	return "No faculty found matching '" + name + "'.";
	}
	
	DButil.Faculty f = list.get(0);
	
	return "Faculty Details:<br> Name: " + f.firstname + " " + f.lastname +
	"<br> Department: " + f.department +
	"<br> Designation: " + f.designation;
	}
	
	/* ==========================================================
	    EVENT HANDLER (YEAR ONLY)
	========================================================== */
	private String processEvent(RasaClient.RasaResult rasa, HttpSession session) {
	
	String yearStr = null;
	
	// First, try to get year from entities map
	if (rasa.entities.containsKey("year")) {
	yearStr = rasa.entities.get("year");
	} else if (rasa.entities.containsKey("years")) {
	yearStr = rasa.entities.get("years");
	} else {
	// fallback: parse a number from user text
	int y = parseInteger(rasa.text);
	if (y > 1900 && y < 3000) yearStr = String.valueOf(y);
	}
	
	if (yearStr == null) {
	session.setAttribute("neededInfo", "year");
	session.setAttribute("pendingIntent", "event_query");
	return "Please provide the year of the event.";
	}
	
	DButil.EventMeta event = DButil.findEvent(yearStr);
	
	if (event == null) {
	return "No event found for the year " + yearStr + ".";
	}
	
	return "Event Details:<br>Event: " + event.eventName +
	"<br>Date: " + event.eventDate +
	"<br>Department: " + event.deptName;
	}

    /* ==========================================================
                          UTILITY
       ========================================================== */
    private int parseInteger(String s) {
        if (s == null) return -1;
        try {
            String num = s.replaceAll("[^0-9]", "");
            if (num.isEmpty()) return -1;
            return Integer.parseInt(num);
        } catch (Exception e) {
            return -1;
        }
    }
}
