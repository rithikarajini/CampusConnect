import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/exam")
public class exam extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        /* ===== NEW VALUES ===== */
        String courseCode = request.getParameter("course_code");
        String courseName = request.getParameter("course_name");
        String examDate   = request.getParameter("exam_date");
        String classes    = request.getParameter("classes");
        int deptId        = Integer.parseInt(request.getParameter("dept_id"));
        String semester   = request.getParameter("semester");
        String examType   = request.getParameter("exam_type");

        /* ===== ORIGINAL VALUES (ONLY PRESENT DURING EDIT) ===== */
        String origCode = request.getParameter("original_course_code");
        String origDate = request.getParameter("original_exam_date");
        String origDept = request.getParameter("original_dept_id");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            /* ================= UPDATE ================= */
            if (origCode != null && !origCode.isBlank()) {

                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE exam SET course_code=?, course_name=?, exam_date=?, classes=?, dept_id=?, exam_type=?, semester=? " +
                    "WHERE course_code=? AND exam_date=? AND dept_id=?"
                );

                ps.setString(1, courseCode);
                ps.setString(2, courseName);
                ps.setDate(3, Date.valueOf(examDate));
                ps.setString(4, classes);
                ps.setInt(5, deptId);
                ps.setString(6, examType);
                ps.setString(7, semester);

                ps.setString(8, origCode);
                ps.setDate(9, Date.valueOf(origDate));
                ps.setInt(10, Integer.parseInt(origDept));

                ps.executeUpdate();
            }

            /* ================= INSERT ================= */
            else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO exam (course_code, course_name, exam_date, classes, dept_id, exam_type, semester) " +
                    "VALUES (?,?,?,?,?,?,?)"
                );
                ps.setString(1, courseCode);
                ps.setString(2, courseName);
                ps.setDate(3, Date.valueOf(examDate));
                ps.setString(4, classes);
                ps.setInt(5, deptId);
                ps.setString(6, examType);
                ps.setString(7, semester);

                ps.executeUpdate();
            }

            response.sendRedirect("./admin_panel/home.html?menu=Exam");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
