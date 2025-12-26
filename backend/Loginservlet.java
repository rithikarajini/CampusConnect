import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Loginservlet")
public class Loginservlet extends HttpServlet {

    private static final String LOGIN_QUERY =
        "SELECT ID, USERNAME, PASSWORD FROM REGICC WHERE USERNAME=? AND PASSWORD=?";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

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

                // create session
                HttpSession session = request.getSession();
                session.setAttribute("id", id);
                session.setAttribute("username", uname);

                out.println("<h2>Login Successful</h2>");
                out.println("<p>Welcome " + uname + " (ID: " + id + ")</p>");
                out.println("<a href='add.html'>Go to Home</a>");

            } else {
                out.println("<h2>Invalid Username or Password</h2>");
                out.println("<a href='logincbhtml.html'>Try Again</a>");
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h2>Error: " + e.getMessage() + "</h2>");
        }
    }
}

