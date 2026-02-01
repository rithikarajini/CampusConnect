import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/fees")
public class fees extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String feesId = request.getParameter("fees_id");
        int amount = Integer.parseInt(request.getParameter("amount"));
        java.sql.Date lastDate = java.sql.Date.valueOf(request.getParameter("last_date"));
        int semester = Integer.parseInt(request.getParameter("semester"));
        int year = Integer.parseInt(request.getParameter("year"));

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            if (feesId != null && !feesId.isBlank()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE fees SET amount=?, last_date=?, semester=?, year=? WHERE fees_id=?"
                );
                ps.setInt(1, amount);
                ps.setDate(2, lastDate);
                ps.setInt(3, semester);
                ps.setInt(4, year);
                ps.setInt(5, Integer.parseInt(feesId));
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO fees (amount, last_date, semester, year) VALUES (?,?,?,?)"
                );
                ps.setInt(1, amount);
                ps.setDate(2, lastDate);
                ps.setInt(3, semester);
                ps.setInt(4, year);
                ps.executeUpdate();
            }

            response.sendRedirect("./admin_panel/home.html?menu=Fees");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
