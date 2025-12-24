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

@WebServlet("/Exam")  // This must match your form action
public class exam extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        // Get parameters from form
        String courseCode = request.getParameter("course_code");
        String courseName = request.getParameter("course_name");
        String examDate = request.getParameter("exam_date");
        String cls = request.getParameter("class");
        String deptId = request.getParameter("dept_id");
        String examType = request.getParameter("exam_type");

        // Basic validation
        if(courseCode == null || courseName == null || examDate == null ||
           cls == null || deptId == null || examType == null ||
           courseCode.isEmpty() || courseName.isEmpty() || examDate.isEmpty() ||
           cls.isEmpty() || deptId.isEmpty() || examType.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("All fields are required.");
            return;
        }

        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to DB
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                // Insert SQL
                String sql = "INSERT INTO exam (course_code, course_name, exam_date, class, dept_id, exam_type) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, courseCode);
                    ps.setString(2, courseName);
                    ps.setString(3, examDate);
                    ps.setString(4, cls);
                    ps.setString(5, deptId);
                    ps.setString(6, examType);

                    int result = ps.executeUpdate();
                    if(result > 0) {
                        out.println("Exam added successfully.");
                    } else {
                        out.println("Failed to add exam.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }
}
