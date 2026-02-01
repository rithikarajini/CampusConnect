import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/home_faculty")
public class home_faculty extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    private String getDeptName(int d) {
        switch (d) {
        case 1: return "BCA";
        case 2: return "MSc IT";
        case 3: return "BCom General";
        case 4: return "BA Corporate Economics";
        case 5: return "BSc Visual Communication";
        case 6: return "BSc IT";
        case 7: return "BSc Psychology";
        case 8: return "BCom A&F";
        case 9: return "BCom CA";
        case 10: return "BCom Honours";
        case 11: return "BBA";
        case 12: return "MA Communication";
        case 13: return "MA HRM";
        case 14: return "MA English";
        case 15: return "MA International Studies";
        case 16: return "MSc Mathematics";
        case 17: return "MSc Physics";
        case 18: return "MSc Chemistry";
        case 19: return "MSc Biotechnology";
        case 21: return "MSc Data Science";
        case 22: return "MCom General";
        case 23: return "BA History";
        case 24: return "BA English";
        case 25: return "BSc Mathematics";
        case 26: return "BSc Physics";
        case 27: return "BSc Chemistry";
        case 28: return "BSc Plant Biology";
        case 29: return "BSc Home Science";
        case 30: return "BSc Computer Science";
        case 31: return "MSc Psychology";
        default: return "UNKNOWN";
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String facultyId = request.getParameter("f_id");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            /* ========= JSON MODE (EDIT) ========= */
            if (facultyId != null) {

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();

                PreparedStatement ps = conn.prepareStatement(
                    "SELECT f_id, Firstname, lastname, designation, dept_id FROM faculty WHERE f_id=?"
                );
                ps.setInt(1, Integer.parseInt(facultyId));

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.print("{");
                    out.print("\"f_id\":" + rs.getInt("f_id") + ",");
                    out.print("\"fname\":\"" + rs.getString("Firstname") + "\",");
                    out.print("\"lname\":\"" + rs.getString("lastname") + "\",");
                    out.print("\"desig\":\"" + rs.getString("designation") + "\",");
                    out.print("\"dept_id\":" + rs.getInt("dept_id"));
                    out.print("}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            /* ========= TABLE MODE ========= */
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT f_id, Firstname, lastname, designation, dept_id FROM faculty ORDER BY f_id");

            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>First</th><th>Last</th><th>Designation</th><th>Dept</th><th>Actions</th></tr>");

            int i = 1;
            while (rs.next()) {
                int f_id = rs.getInt("f_id");
                int deptId = rs.getInt("dept_id");

                out.println("<tr>");
                out.println("<td>" + i++ + "</td>");
                out.println("<td>" + rs.getString("Firstname") + "</td>");
                out.println("<td>" + rs.getString("lastname") + "</td>");
                out.println("<td>" + rs.getString("designation") + "</td>");
                out.println("<td>" + getDeptName(deptId) + "</td>");
                out.println("<td>");

                out.println("<a class='action-edit' href='/CampusConnect/admin_panel/Faculty.html?f_id=" + f_id + "'>");
                out.println("<i class='fa-solid fa-pen-to-square'></i></a> ");

                out.println("<span class='delete-btn' data-type='faculty' data-id='" + f_id + "'>");
                out.println("<i class='fa-solid fa-trash' style='color:red;'></i>");
                out.println("</span>");

                out.println("</td></tr>");
            }
            out.println("</table>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("delete".equals(request.getParameter("action"))) {
            int id = Integer.parseInt(request.getParameter("f_id"));

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM faculty WHERE f_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            response.sendRedirect("/CampusConnect/admin_panel/home.html?menu=Faculty");
        }
    }
}
