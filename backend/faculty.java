import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/faculty")
public class faculty extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String facultyId = request.getParameter("f_id");
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String desig = request.getParameter("desig");
        int dept = Integer.parseInt(request.getParameter("dept"));

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            if (facultyId != null && !facultyId.isBlank()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE faculty SET Firstname=?, lastname=?, designation=?, dept_id=? WHERE f_id=?"
                );
                ps.setString(1, fname);
                ps.setString(2, lname);
                ps.setString(3, desig);
                ps.setInt(4, dept);
                ps.setInt(5, Integer.parseInt(facultyId));
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO faculty (Firstname, lastname, designation, dept_id) VALUES (?,?,?,?)"
                );
                ps.setString(1, fname);
                ps.setString(2, lname);
                ps.setString(3, desig);
                ps.setInt(4, dept);
                ps.executeUpdate();
            }

            response.sendRedirect("./admin_panel/home.html?menu=Faculty");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
