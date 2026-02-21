import java.io.IOException;

import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Loginservlet")
public class Loginservlet extends HttpServlet {

    private static final String LOGIN_QUERY =
        "SELECT ID, USERNAME FROM REGICC WHERE USERNAME=? AND PASSWORD=?";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/campusconnect",
                "root",
                "25swathi14"
            );

            PreparedStatement ps = conn.prepareStatement(LOGIN_QUERY);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                

                int id = rs.getInt("ID");
                String uname = rs.getString("USERNAME");

                HttpSession session = request.getSession();
                session.setAttribute("id", id);
                session.setAttribute("username", uname);

                
                response.sendRedirect("./admin_panel/home.html");

            } else {
                

                request.setAttribute("error", "Invalid Username or Password");

                RequestDispatcher rd =
                        request.getRequestDispatcher("logincbhtml.html");
                rd.forward(request, response);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Server Error! Please try again.");
            RequestDispatcher rd =
                    request.getRequestDispatcher("logincbhtml.html");
            rd.forward(request, response);
        }
    }
}
