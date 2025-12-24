import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/fees")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,   // 2MB
    maxFileSize = 1024 * 1024 * 10,        // 10MB
    maxRequestSize = 1024 * 1024 * 50      // 50MB
)
public class fees extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String amountStr = request.getParameter("amount");
        String lastDateStr = request.getParameter("last_date");
        String semesterStr = request.getParameter("semester");

        // ✅ BASIC VALIDATION
        if (amountStr == null || lastDateStr == null || semesterStr == null ||
            amountStr.isEmpty() || lastDateStr.isEmpty() || semesterStr.isEmpty()) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "All fields are required");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            int semester = Integer.parseInt(semesterStr);
            java.sql.Date lastDate = java.sql.Date.valueOf(lastDateStr);

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn =
                         DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement ps =
                         conn.prepareStatement(
                             "INSERT INTO fees (amount, last_date, semester) VALUES (?, ?, ?)")) {

                ps.setInt(1, amount);
                ps.setDate(2, lastDate);
                ps.setInt(3, semester);

                ps.executeUpdate();
            }

            // ✅ AFTER SUCCESS → BACK TO FEES PAGE
            response.sendRedirect("add.html?menu=Fees");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid number format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error while adding fee");
        }
    }
}
