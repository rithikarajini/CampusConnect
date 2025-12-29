import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home_faculty")
public class home_faculty extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

    // Department mapping
    private String getDeptName(int deptId) {
        switch (deptId) {
            case 1: return "BCA";
            case 2: return "Languages";
            case 3: return "BA Tamil";
            case 4: return "MA English";
            case 5: return "BA English";
            case 6: return "B.Com general A";
            case 7: return "B.Com general B";
            case 8: return "B.Com A&F";
            case 9: return "B.Com CA";
            case 10: return "B.Com Honours";
            case 11: return "M.Com General";
            case 12: return "B.Com CS";
            case 13: return "BBA";
            case 14: return "MA HRM";
            case 15: return "B.SC IT";
            case 16: return "M.SC IT";
            case 17: return "B.SC CS";
            case 18: return "BA Corporate Economics";
            case 19: return "Viscom";
            case 20: return "B.SC Psychology";
            case 21: return "B.SC Data Science";
            case 22: return "MA Communication";
            case 23: return "B.SC Maths";
            case 24: return "M.SC Maths";
            case 25: return "B.SC Physics";
            case 26: return "B.SC Chemistry";
            case 27: return "B.SC Plant Biology";
            case 28: return "B.SC Home Science";
            case 29: return "BA History";
            case 30: return "B.SC Advanced Zoology";
            default: return "UNKNOWN";
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String fIdParam = request.getParameter("f_id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            /* ================= EDIT FORM ================= */
            if (fIdParam != null) {
                int f_id = Integer.parseInt(fIdParam);
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT f_id, Firstname, lastname, designation, dept_id FROM faculty WHERE f_id=?");
                ps.setInt(1, f_id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.println("<h3>Edit Faculty</h3>");
                    out.println("<form method='post' action='home_faculty'>");
                    out.println("<input type='hidden' name='faculty_id' value='" + rs.getInt("f_id") + "'>");
                    out.println("First Name:<input type='text' name='fname' value='" + rs.getString("Firstname") + "' required><br>");
                    out.println("Last Name:<input type='text' name='lname' value='" + rs.getString("lastname") + "' required><br>");
                    out.println("Designation:<input type='text' name='desig' value='" + rs.getString("designation") + "' required><br>");

                    // Department dropdown
                    int currentDept = rs.getInt("dept_id");
                    out.println("Department:<select name='dept'>");
                    for (int d = 1; d <= 30; d++) {
                        if (d == currentDept)
                            out.println("<option value='" + d + "' selected>" + getDeptName(d) + "</option>");
                        else
                            out.println("<option value='" + d + "'>" + getDeptName(d) + "</option>");
                    }
                    out.println("</select><br>");

                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close();
                ps.close();
            }

            /* ================= TABLE LIST ================= */
            else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT f_id, Firstname, lastname, designation, dept_id FROM faculty ORDER BY f_id");

                out.println("<table border='1'>");
                out.println("<tr>");
                out.println("<th>#</th>");
                out.println("<th>First Name</th>");
                out.println("<th>Last Name</th>");
                out.println("<th>Designation</th>");
                out.println("<th>Department</th>");
                out.println("<th>Actions</th>");
                out.println("</tr>");

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
                    out.println("<a class='action-edit' href='home_faculty?f_id=" + f_id + "'>");
                    out.println("<i class='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a class='action-delete' data-f_id='" + f_id + "'>");
                    out.println("<i class='fa-solid fa-trash' style='color:red;'></i></a>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                rs.close();
                stmt.close();

                // DELETE JS
                out.println("<script>");
                out.println("document.querySelectorAll('.action-delete').forEach(btn=>{");
                out.println("btn.onclick=function(e){");
                out.println("e.preventDefault();");
                out.println("const f_id=this.dataset.f_id;");
                out.println("if(!confirm('Delete this faculty?')) return;");
                out.println("const form=document.createElement('form');");
                out.println("form.method='post'; form.action='home_faculty';");
                out.println("form.innerHTML=`<input type='hidden' name='action' value='delete'>"+
                            "<input type='hidden' name='faculty_id' value='${f_id}'>`;");
                out.println("document.body.appendChild(form); form.submit();");
                out.println("};});");
                out.println("</script>");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR: " + e.getMessage());
        }
    }

    /* ================= POST : ADD / UPDATE / DELETE ================= */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // DELETE
        if ("delete".equals(action)) {
            int f_id = Integer.parseInt(request.getParameter("faculty_id"));
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM faculty WHERE f_id=?")) {
                ps.setInt(1, f_id);
                ps.executeUpdate();
                response.sendRedirect("home.html?menu=Faculty");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // ADD / UPDATE
        String fIdParam = request.getParameter("faculty_id");
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String designation = request.getParameter("desig");
        String dept = request.getParameter("dept");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            if (fIdParam != null && !fIdParam.isEmpty()) {
                // UPDATE
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE faculty SET Firstname=?, lastname=?, designation=?, dept_id=? WHERE f_id=?");
                ps.setString(1, fname);
                ps.setString(2, lname);
                ps.setString(3, designation);
                ps.setInt(4, Integer.parseInt(dept));
                ps.setInt(5, Integer.parseInt(fIdParam));
                ps.executeUpdate();
            } else {
                // INSERT
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO faculty (Firstname, lastname, designation, dept_id) VALUES (?,?,?,?)");
                ps.setString(1, fname);
                ps.setString(2, lname);
                ps.setString(3, designation);
                ps.setInt(4, Integer.parseInt(dept));
                ps.executeUpdate();
            }
            response.sendRedirect("home.html?menu=Faculty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
