import jakarta.servlet.ServletContext;

import jakarta.servlet.annotation.WebServlet;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.util.ArrayList;


@WebServlet("/DButil")
public class DButil extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

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
    public static Integer getDeptIdByName(String dept) {
        String sql = "SELECT dept_id FROM dept WHERE LOWER(dept_name) = ?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, dept.toLowerCase());
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
    public static List<Faculty> getFaculty(
            String firstname,
            String lastname,
            String designation,
            Integer deptId
    ) {

        List<Faculty> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT f.Firstname, f.lastname, f.designation, d.dept_name " +
            "FROM faculty f " +
            "JOIN dept d ON f.dept_id = d.dept_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Match by first name
        if (firstname != null) {
            sql.append(" AND f.Firstname LIKE ? ");
            params.add(firstname + "%");
        }

        // Match by last name
        if (lastname != null) {
            sql.append(" AND f.lastname LIKE ? ");
            params.add(lastname + "%");
        }

        // Match by designation
        if (designation != null) {
            sql.append(" AND f.designation = ? ");
            params.add(designation);
        }

        // Match by department
        if (deptId != null) {
            sql.append(" AND f.dept_id = ? ");
            params.add(deptId);
        }
        

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            // Set parameters dynamically
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Faculty f = new Faculty();
                f.Firstname = rs.getString("Firstname"); // capital F as in DB
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
        public String Firstname;   // capital F to match DB
        public String lastname;
        public String designation;
        public String department;
    }


     /* ===========================================================
                             EVENT
        =========================================================== */
    public static List<Event> getEvent(String eventName, Integer deptId) {

        List<Event> list = new ArrayList<>();

        String sql =
            "SELECT e.event_name, e.event_date, e.year, d.dept_name, e.rulebook " +
            "FROM event e " +
            "JOIN dept d ON e.dept_id = d.dept_id " +
            "WHERE LOWER(e.event_name) LIKE ?";

        if (deptId != null) {
            sql += " AND e.dept_id = ?";
        }

        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, "%" + eventName.toLowerCase() + "%");

            if (deptId != null) {
                ps.setInt(i++, deptId);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event e = new Event();
                e.eventName = rs.getString("event_name");
                e.eventDate = rs.getString("event_date") != null
                                ? rs.getDate("event_date").toString()
                                : "";
                e.year = rs.getInt("year");
                e.department = rs.getString("dept_name");
                e.rulebook = rs.getString("rulebook");
                list.add(e);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }
    public static class Event {
        public String eventName;
        public String eventDate;
        public int year;
        public String department;
        public String rulebook;
    }
    
    public static Integer getEventId(String eventName, Integer deptId) {

        Integer eventId = null;

        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(
            		 "SELECT event_id FROM event WHERE LOWER(event_name) LIKE ? AND dept_id=?"
)) {

        	ps.setString(1, "%" + eventName.toLowerCase() + "%");
            ps.setInt(2, deptId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                eventId = rs.getInt("event_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return eventId;
    }
    
    public static String extractCategoryFromMessage(Integer eventId, String message) {

        String categoryName = null;

        String sql = "SELECT category_name FROM event_rule_category WHERE event_id = ?";

        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String category = rs.getString("category_name");

                if (message.contains(category.toLowerCase())) {
                    categoryName = category;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryName;
    }
    
    
    public static List<Rule> getRules(Integer eventId, String categoryName) {

        List<Rule> list = new ArrayList<>();

        String sql =
            "SELECT r.rule_no, r.rule_text " +
            "FROM event_rules r " +
            "JOIN event_rule_category c ON r.category_id = c.category_id " +
            "WHERE c.event_id = ? AND LOWER(c.category_name) = ? " +
            "ORDER BY r.rule_no";

        try (Connection con = getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ps.setString(2, categoryName.toLowerCase());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Rule r = new Rule(
                        rs.getInt("rule_no"),
                        rs.getString("rule_text")
                );

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public static class Rule {
        public int ruleNo;
        public String ruleText;

        public Rule(int ruleNo, String ruleText) {
            this.ruleNo = ruleNo;
            this.ruleText = ruleText;
        }
    }




}

   
