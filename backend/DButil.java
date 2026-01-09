import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
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
            String subject,
            int deptId,
            String examType,
            int semester,
            int classYear
    ) {

        List<Exam> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
        	    "SELECT e.*, d.dept_name " +
        	    "FROM exam e JOIN dept d ON e.dept_id = d.dept_id " +
        	    "WHERE e.dept_id = ? " +
        	    "AND e.exam_type = ? " +
        	    "AND ( ? IS NULL OR LOWER(e.course_name) LIKE ? )"
        	);

        	if (semester != -1) {
        	    sql.append(" AND e.semester = ?");
        	}

        	if (classYear != -1) {
        	    sql.append(" AND e.classes = ?");
        	}

           

        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

        	int idx = 1;

        	// dept
        	ps.setInt(idx++, deptId);

        	// exam type
        	ps.setString(idx++, examType);

        	// subject (MUST come next)
        	if (subject == null) {
        	    ps.setNull(idx++, Types.VARCHAR);
        	    ps.setNull(idx++, Types.VARCHAR);
        	} else {
        	    ps.setString(idx++, subject);
        	    ps.setString(idx++, "%" + subject.toLowerCase() + "%");
        	}

        	// semester
        	if (semester != -1) {
        	    ps.setInt(idx++, semester);
        	}

        	// class year
        	if (classYear != -1) {
        	    ps.setInt(idx++, classYear);
        	}



            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Exam e = new Exam();
                e.courseName = rs.getString("course_name");
                e.examType = rs.getString("exam_type");
                e.semester = rs.getInt("semester");
                e.classes = rs.getInt("classes");
                e.examDate = rs.getDate("exam_date");
                e.deptName = rs.getString("dept_name");
                e.courseCode = rs.getString("course_code");
                list.add(e);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    /* ===================== DATE BASED SEARCH ===================== */
    public static List<Exam> searchExamsForChatbot(
            Date examDate,
            Integer deptId,
            Integer classYear,
            String examType
    ) {

        List<Exam> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT e.course_code, e.course_name, e.exam_date, e.classes, " +
            "e.semester, e.exam_type, d.dept_name " +
            "FROM exam e " +
            "JOIN dept d ON e.dept_id = d.dept_id " +
            "WHERE e.exam_date = ? AND e.dept_id = ? AND e.classes = ? "
        );

        if (examType != null)
            sql.append("AND e.exam_type = ? ");

        sql.append("ORDER BY e.course_name");

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int i = 1;
            ps.setDate(i++, examDate);
            ps.setInt(i++, deptId);
            ps.setInt(i++, classYear);

            if (examType != null)
                ps.setString(i++, normalizeExamType(examType));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapExam(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ===================== COURSE CODE SEARCH ===================== */
    public static List<Exam> searchCourseCodesBySubjectAndDept(
            String subject,
            Integer deptId
    ) {

        List<Exam> list = new ArrayList<>();

        String sql =
            "SELECT DISTINCT e.course_code, e.course_name, d.dept_name " +
            "FROM exam e " +
            "JOIN dept d ON e.dept_id = d.dept_id " +
            "WHERE e.dept_id = ? " +
            "AND (LOWER(e.course_name) LIKE ? OR LOWER(e.course_code) LIKE ?) " +
            "ORDER BY e.course_name";

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String like = "%" + subject.toLowerCase() + "%";

            ps.setInt(1, deptId);
            ps.setString(2, like);
            ps.setString(3, like);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Exam e = new Exam();
                e.courseCode = rs.getString("course_code");
                e.courseName = rs.getString("course_name");
                e.deptName = rs.getString("dept_name");
                list.add(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ===================== DEPARTMENT LOOKUP ===================== */
    public static Integer getDeptIdByName(String deptFromRasa) {

        if (deptFromRasa == null) return null;

        String norm = deptFromRasa.toLowerCase().replaceAll("[^a-z]", "");

        String sql =
            "SELECT dept_id FROM dept " +
            "WHERE LOWER(REPLACE(REPLACE(REPLACE(dept_name,'.',''),' ',''),'-','')) LIKE ?";

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + norm + "%");

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("dept_id");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* ===================== NORMALIZER ===================== */
    private static String normalizeExamType(String raw) {

        if (raw == null) return null;

        raw = raw.toLowerCase().replaceAll("\\s+", "");

        if (raw.contains("cia1")) return "CIA-1";
        if (raw.contains("cia2")) return "CIA-2";
        if (raw.contains("semester")) return "SEMESTER";

        return raw.toUpperCase();
    }

    /* ===================== MAPPER ===================== */
    private static Exam mapExam(ResultSet rs) throws SQLException {

        Exam e = new Exam();
        e.courseCode = rs.getString("course_code");
        e.courseName = rs.getString("course_name");
        e.examDate   = rs.getDate("exam_date");
        e.classes    = rs.getInt("classes");
        e.semester   = rs.getInt("semester");
        e.examType   = rs.getString("exam_type");
        e.deptName   = rs.getString("dept_name");
        return e;
    }

    /* ===================== MODEL ===================== */
    public static class Exam {
        public String courseCode;
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
    	
        boolean hasSemester = semester != -1;

        List<Fee> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT amount, semester, year, last_date FROM fees WHERE year = ?"
        );

        if (hasSemester) {
            sql.append(" AND semester = ?");
        }

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int index = 1;

            // Always bind year
            ps.setInt(index++, years);

            if (hasSemester) {
                ps.setInt(index++, semester);
            }

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
}

   