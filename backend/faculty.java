import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Faculty")
public class faculty extends HttpServlet {

    private static final String JDBC_URL =
        "jdbc:mysql://localhost:3306/campusconnect";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String designation = request.getParameter("designation");
        String department = request.getParameter("department");

        // DEBUG – MUST SHOW VALUES
        System.out.println(name);
        System.out.println(designation);
        System.out.println(department);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    JDBC_URL, DB_USER, DB_PASSWORD);

            String sql =
              "INSERT INTO faculty(name, designation, department) VALUES (?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, designation);
            ps.setString(3, department);

            ps.executeUpdate();
            conn.close();

            response.getWriter().println("Faculty Added Successfully");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error");
        }
    }
}
