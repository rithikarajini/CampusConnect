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

    private static final String URL =
            "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String amountStr   = request.getParameter("amt");
        String lastDateStr = request.getParameter("lst_date");
        String semesterStr = request.getParameter("sem");

        // Basic validation
        if (amountStr == null || lastDateStr == null || semesterStr == null ||
            amountStr.isEmpty() || lastDateStr.isEmpty() || semesterStr.isEmpty()) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "All fields are required");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            int semester = Integer.parseInt(semesterStr);

            // Convert date
            java.sql.Date lastDate = java.sql.Date.valueOf(lastDateStr);

            // Extract year safely
            int year = lastDate.toLocalDate().getYear();

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO fees (amount, last_date, semester, year) VALUES (?, ?, ?, ?)")) {

                ps.setInt(1, amount);
                ps.setDate(2, lastDate);
                ps.setInt(3, semester);
                ps.setInt(4, year);

                ps.executeUpdate();
            }

            // After success → go back to Fees page
            response.sendRedirect("admin_panel/home.html?menu=Fees");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Amount and Semester must be numbers");
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid date format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error while adding fee");
        }
    }
}
