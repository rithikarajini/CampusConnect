
import java.io.IOException;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/faculty")
public class faculty extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String designation = request.getParameter("desig");
        String department = request.getParameter("dept");

        

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    URL, USER, PASS);

            String sql = "INSERT INTO faculty(Firstname, lastname, designation, dept_id) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setString(3, designation);
            ps.setString(4, department);
            ps.executeUpdate();
            conn.close();

            response.sendRedirect("home.html?menu=Faculty");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error");
        }
    }
}
