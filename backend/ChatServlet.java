import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/ChatServlet")
public class ChatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    public static String handleChat(String userMsg, HttpSession session) {

        if (userMsg == null || userMsg.trim().isEmpty()) {
            return "I didn't hear anything.";
        }

        String normalized = userMsg.toLowerCase();

        // 🔥 HARD COURSE CODE ROUTE (works for voice + typing)
        if (normalized.contains("course code")) {
            session.removeAttribute("neededInfo");
            session.removeAttribute("pendingIntent");

            RasaClient.RasaResult rasa = RasaClient.interpret(userMsg);
            return new ChatServlet().processCourseCode(rasa, session);
        }

        String needed = (String) session.getAttribute("neededInfo");
        String intent = (String) session.getAttribute("pendingIntent");

        if (needed != null && intent != null) {
            return new ChatServlet().handleFollowUp(userMsg, session);
        }

        RasaClient.RasaResult rasa = RasaClient.interpret(userMsg);
        return new ChatServlet().handleIntent(rasa, session);
    }



    /* ===================== ENTRY ===================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
    	
    	 HttpSession session = req.getSession();
    	    String userMsg = req.getParameter("message");

    	    String reply = handleChat(userMsg, session);

    	    resp.setContentType("text/html");
    	    resp.setCharacterEncoding("UTF-8");
    	    resp.getWriter().write(reply);
    }
    // ================== CENTRALIZED CHATBOT LOGIC ==================
  	public static String processMessage(String userMsg, HttpSession session) {

  	    if (userMsg == null || userMsg.trim().isEmpty()) {
  	        return "I didn't hear anything.";
  	    }

  	    String needed = (String) session.getAttribute("neededInfo");
  	    String intent = (String) session.getAttribute("pendingIntent");

  	    if (needed != null && intent != null) {
  	        return new ChatServlet().handleFollowUp(userMsg, session);
  	    } else {
  	        RasaClient.RasaResult rasa = RasaClient.interpret(userMsg);
  	        return new ChatServlet().handleIntent(rasa, session);
  	    }
  	}

    /* ===================== INTENT ROUTER ===================== */
    private String handleIntent(RasaClient.RasaResult rasa, HttpSession session) {

        if (rasa == null || rasa.intent == null)
            return "I couldn’t understand that. Please try again.";

        switch (rasa.intent) {

            case "exam_schedule":
                return processExam(rasa, session);

            case "fees_query":
                return processFees(rasa, session);

            case "greet":
                return "Hello! How can I help you today?";

            case "goodbye":
                return "Thank you! Feel free to ask anytime.";

            default:
                return "I’m not sure I understood that. Can you rephrase?";
        }
    }

    /* ===================== FOLLOW-UP ===================== */
    private String handleFollowUp(String msg, HttpSession session) {

        String need = (String) session.getAttribute("neededInfo");
        String intent = (String) session.getAttribute("pendingIntent");

        if (need == null || intent == null) {
            return "Please ask your question again.";
        }

        RasaClient.RasaResult r = new RasaClient.RasaResult();
        r.intent = intent;
        r.text = msg;
        r.entities = new HashMap<>();

        /* ---------- Map follow-up to entity ---------- */
        switch (need) {

            case "examType" -> r.text = msg;

            case "semester" -> r.entities.put("semester", msg);

            case "year" -> r.entities.put("year", msg);

            case "department" -> r.entities.put("dept", msg);

            case "classYear" -> r.text = msg;

            case "subject" -> r.entities.put("subject", msg);

            default -> r.text = msg;
        }

        /* ---------- Clear follow-up state ---------- */
        session.removeAttribute("neededInfo");
        session.removeAttribute("pendingIntent");

        /* ---------- Route to correct processor ---------- */
        switch (intent) {

            case "exam_schedule":
                return processExam(r, session);

            case "fees_query":
                return processFees(r, session);
                
            case "course_code":
                return processCourseCode(r, session);


           /* case "event_query":
                return processEvent(r, session);*/

            default:
                return "I have the information, but I’m not sure how to process it.";
        }
    }



    /* ===================== FEES ===================== */
    private String processFees(RasaClient.RasaResult rasa, HttpSession session) {

        // ---------- LOAD FROM SESSION ----------
        Integer sem = (Integer) session.getAttribute("fee_semester");
        Integer year = (Integer) session.getAttribute("fee_year");

        // ---------- EXTRACT FROM CURRENT MESSAGE ----------
        int s = extractSemester(rasa);
        if (s != -1) {
            sem = s;
            session.setAttribute("fee_semester", sem);
        }

        int y = extractYear(rasa);
        if (y != -1) {
            year = y;
            session.setAttribute("fee_year", year);
        }

        // ---------- ASK FOR MISSING INFO ----------
        if (year == null) {
            session.setAttribute("neededInfo", "year");
            session.setAttribute("pendingIntent", "fees_query");
            return "Please provide the year.(eg: 2025)";
        }

        if (sem == null) {
            session.setAttribute("neededInfo", "semester");
            session.setAttribute("pendingIntent", "fees_query");
            return "Please provide the semester number (1–6).";
        }

        // ---------- BOTH PRESENT → DB ----------
        List<DButil.Fee> list = DButil.searchFees(year, sem);

        if (list == null || list.isEmpty()) {
            clearFees(session);
            return "No fee details found for Semester " + sem + " in " + year + ".";
        }

        DButil.Fee f = list.get(0);

        clearFees(session);

        return "<b>Fee Details</b><br><hr>"
                + "<b>Year:</b> " + f.years + "<br>"
                + "<b>Semester:</b> " + f.semester + "<br>"
                + "<b>Amount:</b> ₹" + f.amount + "<br>"
                + "<b>Last Date:</b> " + f.lastDate;
    }

    /* ===================== EXAMS ===================== */
    private String processCourseCode(RasaClient.RasaResult rasa, HttpSession session) {

        String subject = (String) session.getAttribute("cc_subject");
        String dept = (String) session.getAttribute("cc_dept");

        String sub = extractSubject(rasa);
        if (sub != null) {
            subject = sub;
            session.setAttribute("cc_subject", sub);
        }

        String d = extractDepartment(rasa);
        if (d != null) {
            dept = d;
            session.setAttribute("cc_dept", d);
        }

        if (subject == null)
            return askCourseCode(session, "subject", "Which subject?");

        if (dept == null)
            return askCourseCode(session, "department", "Which department?");

        Integer deptId = DButil.getDeptIdByName(dept);
        if (deptId == null)
            return resetCourseCode(session, "Invalid department.");

        List<DButil.Exam> exams =
                DButil.searchCourseCodesBySubjectAndDept(subject, deptId);

        if (exams.isEmpty())
            return resetCourseCode(session, "No course code found.");

        clearCourseCode(session);
        return formatCourseCode(exams);
    }

    private String processExam(RasaClient.RasaResult rasa, HttpSession session) {
    	
    	/* ================= LOAD STATE ================= */
    	String subject = (String) session.getAttribute("subject");
    	String dept = (String) session.getAttribute("dept");
        String examType = (String) session.getAttribute("examType");
        Integer semester = (Integer) session.getAttribute("semester");
        Integer classYear = (Integer) session.getAttribute("classYear");
        LocalDate date = (LocalDate) session.getAttribute("date");
    
        String et = extractExamType(rasa);
        if (et != null) {
            examType = et;
            session.setAttribute("examType", et);
        }
  
        int sem = extractSemester(rasa);
        if (sem != -1) {
            semester = sem;
            session.setAttribute("semester", sem);
        }
     // ---------- AUTO INFER CLASS YEAR FROM SEMESTER ----------
        if (semester != null && classYear == null) {
            Integer inferredYear = inferClassYearFromSemester(semester);
            if (inferredYear != null) {
                classYear = inferredYear;
                session.setAttribute("classYear", inferredYear);
            }
        }

        boolean subjectSpecified = false;

        String sub = extractSubject(rasa);
        if (sub != null) {
            subject = sub;
            subjectSpecified = true;
            session.setAttribute("subject", sub);
        } else if (subject != null) {
            subjectSpecified = true;
        }

    	String d = extractDepartment(rasa);
    	if (d != null) {
    	    dept = d;
    	    session.setAttribute("dept", d);
    	}


        Integer cy = extractClassYear(rasa);
        if (cy != null) {
            classYear = cy;
            session.setAttribute("classYear", cy);
        }

        LocalDate dt = extractDate(rasa);
        if (dt != null) {
            date = dt;
            session.setAttribute("date", dt);
        }

     
        
       

        /* ================= DATE RULE ================= */
        if (date != null) {

            if (dept == null)
                return ask(session, "department", "Which department?");

            if (classYear == null)
                return ask(session, "classYear", "Which class year? (eg: 1st year , 2nd year , 3rd year)");
            Integer deptId = DButil.getDeptIdByName(dept);
            if (deptId == null)
                return resetExam(session, "Invalid department.");

            List<DButil.Exam> exams =
                    DButil.searchExamsForChatbot(
                            java.sql.Date.valueOf(date),
                            deptId,
                            classYear,
                            examType
                    );

            if (exams.isEmpty())
                return resetExam(session, "No exams found on that date.");

            clearExam(session);
            return formatExam(exams);
        }

        /* ================= GENERAL RULE ================= */
        if (examType == null)
            return ask(session, "examType", "Which exam? CIA-1, CIA-2 or Semester?");

        if ("semester".equals(examType) && semester == null)
            return ask(session, "semester", "Which semester?");

        if (dept == null)
            return ask(session, "department", "Which department?");

        if (classYear == null)
            return ask(session, "classYear", "Which class year? (eg: 1st year , 2nd year , 3rd year)");
        Integer deptId = DButil.getDeptIdByName(dept);
        if (deptId == null)
            return resetExam(session, "Invalid department.");

        List<DButil.Exam> exams =
                DButil.searchExams(
                        subjectSpecified ? subject : null, 
                        deptId,
                        examType,
                        semester != null ? semester : -1,
                        classYear != null ? classYear : -1
                );


        if (exams.isEmpty())
            return resetExam(session, "No exams found.");

        clearExam(session);
        return formatExam(exams);
    }

    

    /* ===================== OUTPUT ===================== */
    private String formatExam(List<DButil.Exam> exams) {

        StringBuilder sb = new StringBuilder("<b>Exam Schedule</b><br><br>");

        for (DButil.Exam e : exams) {
            sb.append("<hr>")
              .append("<b>Course Code:</b> ").append(e.courseCode).append("<br>")
              .append("<b>Course Name:</b> ").append(e.courseName).append("<br>")
              .append("<b>Exam Type:</b> ").append(e.examType).append("<br>")
              .append("<b>Department:</b> ").append(formatDeptForDisplay(e.deptName)).append("<br>")
              .append("<b>Semester:</b> ").append(e.semester).append("<br>")
              .append("<b>Class:</b> ").append(e.classes).append("<br>")
              .append("<b>Date:</b> ").append(e.examDate).append("<br>");
        }
        return sb.toString();
    }
    private String formatCourseCode(List<DButil.Exam> exams) {

        DButil.Exam e = exams.get(0);

        return "<b>Course Code Details</b><br><hr>" +
               "<b>Course Name:</b> " + e.courseName + "<br>" +
               "<b>Course Code:</b> " + e.courseCode + "<br>" +
               "<b>Department:</b> " + formatDeptForDisplay(e.deptName);
    }

    /* ===================== HELPERS ===================== */
    private Integer inferClassYearFromSemester(Integer semester) {
        if (semester == null) return null;

        if (semester == 1 || semester == 2) return 1;
        if (semester == 3 || semester == 4) return 2;
        if (semester == 5 || semester == 6) return 3;

        return null; // invalid semester
    }
    
    private String formatDeptForDisplay(String dept) {

        if (dept == null) return "";

        switch (dept.toLowerCase()) {
            case "mscit":
                return "MSc IT";

            case "bcomgeneral":
                return "BCom General";

            case "bca":
                return "BCA";

            case "bscit":
                return "BSc IT";

            default:
                // Generic fallback: capitalize words
                return dept.substring(0, 1).toUpperCase() + dept.substring(1);
        }
    }


    private String askCourseCode(HttpSession s, String need, String msg) {
        s.setAttribute("neededInfo", need);
        s.setAttribute("pendingIntent", "course_code");
        return msg;
    }

    private String resetCourseCode(HttpSession s, String msg) {
        clearCourseCode(s);
        return msg;
    }

    private void clearCourseCode(HttpSession s) {
        s.removeAttribute("cc_subject");
        s.removeAttribute("cc_dept");
        s.removeAttribute("neededInfo");
        s.removeAttribute("pendingIntent");
    }

    private String ask(HttpSession s, String need, String msg) {
        s.setAttribute("neededInfo", need);
        s.setAttribute("pendingIntent", "exam_schedule");
        return msg;
    }

    private String resetExam(HttpSession s, String msg) {
        clearExam(s);
        return msg;
    }

    private void clearExam(HttpSession s) {
        s.removeAttribute("examType");
        s.removeAttribute("semester");
        s.removeAttribute("dept");
        s.removeAttribute("classYear");
        s.removeAttribute("date");
        s.removeAttribute("subject");
        s.removeAttribute("courseCode");
        s.removeAttribute("neededInfo");
        s.removeAttribute("pendingIntent");
        s.removeAttribute("mode");

    }

    private void clearFees(HttpSession s) {
        s.removeAttribute("neededInfo");
        s.removeAttribute("pendingIntent");
        s.removeAttribute("collectedEntities");
        s.removeAttribute("fee_semester");
        s.removeAttribute("fee_year");
    }


    private int parseNumber(String s) {
        if (s == null) return -1;
        try {
            s = s.replaceAll("[^0-9]", "");
            return s.isEmpty() ? -1 : Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }

    private String extractExamType(RasaClient.RasaResult r) {
        if (r.entities != null && r.entities.containsKey("examType"))
            return normalizeExamType(r.entities.get("examType"));
        return normalizeExamType(r.text);
    }

    private String normalizeExamType(String s) {
        if (s == null) return null;
        s = s.toLowerCase().replaceAll("\\s+", "");
        if (s.contains("cia1")) return "CIA-1";
        if (s.contains("cia2")) return "CIA-2";
        if (s.contains("ciaone")) return "CIA-1";
        if (s.contains("ciatwo")) return "CIA-2";
        if (s.contains("semester")) return "semester";
        return null;
    }

    private int extractSemester(RasaClient.RasaResult r) {
        return r.entities != null ? parseNumber(r.entities.get("semester")) : -1;
    }

    private int extractYear(RasaClient.RasaResult r) {
        return r.entities != null ? parseNumber(r.entities.get("year")) : -1;
    }

    private Integer extractClassYear(RasaClient.RasaResult r) {
        if (r == null || r.text == null) return null;

        String t = r.text.toLowerCase();

        // Only extract if "year" is mentioned
        if (!t.contains("year")) return null;

        if (t.contains("1st") || t.contains("first")) return 1;
        if (t.contains("2nd") || t.contains("second")) return 2;
        if (t.contains("3rd") || t.contains("third")) return 3;

        int n = parseNumber(t);
        return (n >= 1 && n <= 3) ? n : null;
    }


    private String extractDepartment(RasaClient.RasaResult r) {
        if (r.entities == null) return null;
        String d = r.entities.get("dept");
        return d == null ? null : d.toLowerCase();
    }

    private String extractSubject(RasaClient.RasaResult r) {
        if (r.entities == null) return null;
        String s = r.entities.get("subject");
        return (s == null || s.isBlank()) ? null : s.toLowerCase();
    }

    private LocalDate extractDate(RasaClient.RasaResult r) {

        if (r == null || r.entities == null || !r.entities.containsKey("date"))
            return null;

        String raw = r.entities.get("date").trim();

        if (raw.isEmpty()) return null;

        List<DateTimeFormatter> formats = List.of(

            // ISO
            DateTimeFormatter.ISO_LOCAL_DATE,          // 2026-01-16

            // Numeric formats
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d-M-yy"),
            DateTimeFormatter.ofPattern("dd-MM-yy"),

            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),

            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d.M.yy"),
            DateTimeFormatter.ofPattern("dd.MM.yy"),

            // Month name formats
            DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d-MMM-yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH),

            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yy", Locale.ENGLISH),

            DateTimeFormatter.ofPattern("d-MMMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d-MMMM-yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MMMM-yy", Locale.ENGLISH),

            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMMM yy", Locale.ENGLISH),

            // Year-first variants
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy.M.d"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd")
        );

        // Remove ordinal suffixes: 12th → 12
        raw = raw.toLowerCase()
                 .replaceAll("(st|nd|rd|th)", "")
                 .trim();

        for (DateTimeFormatter f : formats) {
            try {
                return LocalDate.parse(raw, f);
            } catch (Exception ignored) {}
        }

        return null;
    }

    }
