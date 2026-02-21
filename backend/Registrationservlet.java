import java.io.IOException;

import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Registrationservlet")
public class Registrationservlet extends HttpServlet {

    private static final String QUERY =
        "INSERT INTO REGICC(ID, USERNAME, EMAIL, PASSWORD, CONFIRM_PASSWORD) VALUES (?, ?, ?, ?, ?)";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        // ❌ optional validation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            RequestDispatcher rd =
                    request.getRequestDispatcher("register.jsp");
            rd.forward(request, response);
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/campusconnect",
                "root",
                "25swathi14"
            );

            PreparedStatement ps = conn.prepareStatement(QUERY);
            ps.setInt(1, id);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setString(5, confirmPassword);

            int count = ps.executeUpdate();

            ps.close();
            conn.close();

            if (count == 1) {
                // ✅ REGISTRATION SUCCESS → LOGIN PAGE
                response.sendRedirect("./admin_panel/logincbhtml.html");
            } else {
                request.setAttribute("error", "Registration Failed");
                RequestDispatcher rd =
                        request.getRequestDispatcher("regich.html");
                rd.forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "User already exists or DB error");
            RequestDispatcher rd =
                    request.getRequestDispatcher("regich.html");
            rd.forward(request, response);
        }
    }
}
