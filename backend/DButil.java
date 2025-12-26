import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.sql.Date;
import java.util.*;

@WebServlet("/DButil")
public class DButil extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String URL = "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /* ===========================================================
                        EVENTS
       =========================================================== */
    public static EventMeta findEvent(String name) {
        String sql =
            "SELECT  e.event_name, e.event_date, d.dept_name, e.rulebook "
          + "FROM event e "
          + "JOIN dept d ON e.dept_id = d.dept_id "
          + "WHERE e.event_name LIKE ? LIMIT 1";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EventMeta e = new EventMeta();
                e.eventName = rs.getString("event_name");
                e.eventDate = rs.getDate("event_date");
                e.deptName = rs.getString("dept_name");
                e.rulebook = rs.getString("rulebook");
                return e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class EventMeta {
        public int id;
        public String eventName;
        public Date eventDate;
        public String deptName;
        public String rulebook;
    }


    /* ===========================================================
                        EXAMS
       =========================================================== */
    public static List<Exam> searchExams(
            String subject, String dept, String examType, int semester) {

        List<Exam> list = new ArrayList<>();

        String sql =
            "SELECT e.course_code, e.course_name, e.exam_date, e.classes, "
          + "e.semester, e.exam_type, d.dept_name "
          + "FROM exam e "
          + "JOIN dept d ON e.dept_id = d.dept_id "
          + "WHERE ( ? = -1 OR e.semester = ? ) "                // semester
          + "AND ( ? = 'ANY' OR e.exam_type LIKE ? ) "           // exam type
          + "AND ( ? = 'ANY' "
          + "       OR e.course_name LIKE ? "
          + "       OR e.course_code LIKE ? ) "                  // subject
          + "AND ( ? = 'ANY' OR d.dept_name LIKE ? ) "           // department
          + "ORDER BY e.exam_date";

        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {

            // Semester
            ps.setInt(1, semester);
            ps.setInt(2, semester);

            // Exam Type
            ps.setString(3, examType);
            ps.setString(4, "%" + examType + "%");

            // Subject
            ps.setString(5, subject);
            ps.setString(6, "%" + subject + "%");
            ps.setString(7, "%" + subject + "%");

            // Department
            ps.setString(8, dept);
            ps.setString(9, "%" + dept + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Exam e = new Exam();
                e.courseCode = rs.getString("course_code");
                e.courseName = rs.getString("course_name");
                e.examDate = rs.getDate("exam_date");
                e.classes = rs.getInt("classes");
                e.semester = rs.getInt("semester");
                e.examType = rs.getString("exam_type");
                e.deptName = rs.getString("dept_name");
                list.add(e);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }
    public static class Exam { public String courseCode;
							    public String courseName;
							    public Date examDate;
							    public int classes;
							    public int semester;
							    public String examType;
							    public String deptName; 
							  }

    /* ===========================================================
                        FEES
       =========================================================== */
    public static List<Fee> searchFees(int years, int semester) {

        // If user did NOT specify year (ex: -1), use current year
        if (years == -1) {
            years = java.time.LocalDate.now().getYear();
        }

        List<Fee> list = new ArrayList<>();

        String sql =
            "SELECT amount, semester, year, last_date " +
            "FROM fees " +
            "WHERE year = ? " +
            "OR (semester = ? OR ? = -1)";

        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, years);        // final year (either user year or current year)

            ps.setInt(2, semester);     // semester filter
            ps.setInt(3, semester);     // used only if NO semester filter

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Fee f = new Fee();
                f.amount = rs.getInt("amount");
                f.semester = rs.getInt("semester");
                f.years = rs.getInt("year");
                f.lastDate = rs.getDate("last_date");
                list.add(f);
            }

        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        return list;
    }

    public static class Fee {
        public int amount;
        public int semester;
        public int years;
        public Date lastDate;
    }


    /* ===========================================================
                        FACULTY
       =========================================================== */
    public static List<Faculty> getFaculty(String keyword) {

        List<Faculty> list = new ArrayList<>();

        // Split keyword by space to check for full name
        String firstNameKeyword = keyword;
        String lastNameKeyword = keyword;

        String[] parts = keyword.trim().split("\\s+");
        if (parts.length >= 2) {
            firstNameKeyword = parts[0];
            lastNameKeyword = parts[1];
        }

        String sql =
            "SELECT f.firstname, f.lastname, f.designation, d.dept_name "
          + "FROM faculty f "
          + "JOIN dept d ON f.dept_id = d.dept_id "
          + "WHERE "
          + "(f.firstname LIKE ? AND f.lastname LIKE ?) "   
          + "OR f.firstname LIKE ? "                      
          + "OR f.lastname LIKE ? "                       
          + "OR f.designation LIKE ? "                     
          + "OR d.dept_name LIKE ?";                      

        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {

            // Full name match
            ps.setString(1, "%" + firstNameKeyword + "%");
            ps.setString(2, "%" + lastNameKeyword + "%");

            // First name only
            ps.setString(3, "%" + firstNameKeyword + "%");

            // Last name only
            ps.setString(4, "%" + lastNameKeyword + "%");

            // Designation
            ps.setString(5, "%" + keyword + "%");

            // Department
            ps.setString(6, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Faculty f = new Faculty();
                f.firstname = rs.getString("firstname");
                f.lastname = rs.getString("lastname");
                f.designation = rs.getString("designation");
                f.department = rs.getString("dept_name");
                list.add(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    public static class Faculty {
        public String firstname;
        public String lastname;
        public String designation;
        public String department;
    }


    /* ===========================================================
                     SERVLET API HANDLER
       =========================================================== */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException { handleRequest(request, response); }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException { handleRequest(request, response); }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        JSONObject result = new JSONObject();

        try {
            switch (action) {

                /* ---------- EVENT ---------- */
        
            case "findEvent":
            	 String ev = request.getParameter("name");
                 EventMeta em = findEvent(ev);

                 if (em != null) {
                     result.put("eventName", em.eventName);
                     result.put("eventDate", em.eventDate.toString());
                     result.put("deptName", em.deptName);

                     String rulebookText = "";
                     if (em.rulebook != null && !em.rulebook.isEmpty()) {
                         String pdfRelative = "/files/" + em.rulebook;
                         String pdfAbsolute = getServletContext().getRealPath(pdfRelative);
                         File pdfFile = new File(pdfAbsolute);
                         if (pdfFile.exists()) {
                             try (PDDocument document = Loader.loadPDF(pdfFile)) {
                                 PDFTextStripper stripper = new PDFTextStripper();
                                 rulebookText = stripper.getText(document);
                             } catch (IOException ex) {
                                 rulebookText = "Error reading PDF: " + ex.getMessage();
                             }
                         } else {
                             rulebookText = "Rulebook PDF not found: " + pdfAbsolute;
                         }
                     }
                     result.put("rulebookText", rulebookText);
                 } else {
                     result.put("error", "Event not found");
                 }
                break;


                /* ---------- EXAMS ---------- */
                case "searchExams":
                    String subject = request.getParameter("subject");
                    String dept = request.getParameter("dept");
                    String examType = request.getParameter("examType");
                    int sem = Integer.parseInt(request.getParameter("semester"));

                    List<Exam> exams = searchExams(subject, dept, examType, sem);
                    JSONArray examArr = new JSONArray();

                    for (Exam e : exams) {
                        JSONObject o = new JSONObject();
                        o.put("courseCode", e.courseCode);
                        o.put("courseName", e.courseName);
                        o.put("examDate", e.examDate.toString());
                        o.put("classes", e.classes);
                        o.put("semester", e.semester);
                        o.put("examType", e.examType);
                        o.put("deptName", e.deptName);
                        examArr.put(o);
                    }

                    result.put("exams", examArr);
                    break;

                /* ---------- FEES ---------- */
                case "searchFees":
                    int year = Integer.parseInt(request.getParameter("year"));
                    int semester = Integer.parseInt(request.getParameter("semester"));

                    List<Fee> fees = searchFees(year, semester);
                    JSONArray feeArr = new JSONArray();

                    for (Fee f : fees) {
                        JSONObject o = new JSONObject();
                        o.put("amount", f.amount);
                        o.put("semester", f.semester);
                        o.put("year", f.years);
                        o.put("lastDate", f.lastDate.toString());
                        feeArr.put(o);
                    }

                    result.put("fees", feeArr);
                    break;

                /* ---------- FACULTY ---------- */
                case "getFaculty":
                    String key = request.getParameter("keyword");
                    List<Faculty> fac = getFaculty(key);

                    JSONArray facArr = new JSONArray();
                    for (Faculty f : fac) {
                        JSONObject o = new JSONObject();
                        o.put("firstname", f.firstname);
                        o.put("lastname",f.lastname);
                        o.put("designation", f.designation);
                        o.put("department", f.department);
                        facArr.put(o);
                    }

                    result.put("faculty", facArr);
                    break;

                default:
                    result.put("error", "Invalid action");
            }

            out.print(result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
