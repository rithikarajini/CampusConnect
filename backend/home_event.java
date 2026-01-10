import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/fees")
public class fees extends HttpServlet {
    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String amountStr   = request.getParameter("amount");
        String lastDateStr = request.getParameter("last_date");
        String semesterStr = request.getParameter("semester");
        String yearStr     = request.getParameter("year");

        if (amountStr == null || lastDateStr == null || semesterStr == null || yearStr == null ||
            amountStr.isEmpty() || lastDateStr.isEmpty() || semesterStr.isEmpty() || yearStr.isEmpty()) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "All fields are required");
            return;
        }

        try {
            int amount = Integer.parseInt(amountStr);
            int semester = Integer.parseInt(semesterStr);
            int year = Integer.parseInt(yearStr);
            java.sql.Date lastDate = java.sql.Date.valueOf(lastDateStr);

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

            // Redirect back to Fees page after success
            response.sendRedirect("home.html?menu=Fees");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Amount, Semester and Year must be numbers");
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error saving fee");
        }
    }
}
