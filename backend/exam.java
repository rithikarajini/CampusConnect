import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/exam")
public class exam extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/demo2?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        // ✅ Parameters EXACTLY match HTML name=""
        String courseCode = request.getParameter("course_code");
        String courseName = request.getParameter("course_name");
        String examDate   = request.getParameter("exam_date");
        String classes    = request.getParameter("class");
        String deptId     = request.getParameter("dept_id");
        String semester   = request.getParameter("semester");
        String examType   = request.getParameter("exam_type");

        // ✅ Validation
        if (courseCode == null || courseCode.trim().isEmpty() ||
            courseName == null || courseName.trim().isEmpty() ||
            examDate == null   || examDate.trim().isEmpty()   ||
            classes == null    || classes.trim().isEmpty()    ||
            deptId == null     || deptId.trim().isEmpty()     ||
            semester == null   || semester.trim().isEmpty()   ||
            examType == null   || examType.trim().isEmpty()) {

            out.println("All fields are required");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

                String sql = "INSERT INTO exam " +
                        "(course_code, course_name, exam_date, classes, dept_id, semester, exam_type) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, courseCode);
                    ps.setString(2, courseName);
                    ps.setString(3, examDate);
                    ps.setString(4, classes);
                    ps.setString(5, deptId);
                    ps.setString(6, semester);
                    ps.setString(7, examType);

                    ps.executeUpdate(); // 🔥 IMPORTANT
                }
            }

            response.sendRedirect("home.html?menu=Exam");

        } catch (Exception e) {
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }
}
