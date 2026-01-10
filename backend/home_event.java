
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home_event")
public class home_event extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false&allowPublicKeyRetrieval=true";
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
        String idParam = request.getParameter("event_id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            /* ================= EDIT FORM ================= */
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM event WHERE event_id=?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.println("<h3>Edit Event</h3>");
                    out.println("<form method='post' action='home_event'>");
                    out.println("<input type='hidden' name='event_id' value='" + rs.getInt("event_id") + "'>");
                    out.println("Event Name:<input type='text' name='event_name' value='" + rs.getString("event_name") + "' required><br>");
                    out.println("Date:<input type='date' name='event_date' value='" + rs.getString("event_date") + "' required><br>");

                    // Department dropdown
                    int currentDept = rs.getInt("dept_id");
                    out.println("Department:<select name='dept_id'>");
                    for (int d = 1; d <= 30; d++) {
                        if (d == currentDept)
                            out.println("<option value='" + d + "' selected>" + getDeptName(d) + "</option>");
                        else
                            out.println("<option value='" + d + "'>" + getDeptName(d) + "</option>");
                    }
                    out.println("</select><br>");

                    out.println("Rulebook:<input type='text' name='rulebook' value='" + rs.getString("rulebook") + "' required><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close();
                ps.close();
            }

            /* ================= TABLE LIST ================= */
            else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM event ORDER BY event_id");
                out.println("<table border='1'>");
                out.println("<tr>");
                out.println("<th>#</th>");
                out.println("<th>Event Name</th>");
                out.println("<th>Date</th>");
                out.println("<th>Department</th>");
                out.println("<th>Rulebook</th>");
                out.println("<th>Actions</th>");
                out.println("</tr>");

                int i = 1;
                while (rs.next()) {
                    int id = rs.getInt("event_id");
                    int deptId = rs.getInt("dept_id");
                    out.println("<tr>");
                    out.println("<td>" + i++ + "</td>");
                    out.println("<td>" + rs.getString("event_name") + "</td>");
                    out.println("<td>" + rs.getString("event_date") + "</td>");
                    out.println("<td>" + getDeptName(deptId) + "</td>");
                    out.println("<td>" + rs.getString("rulebook") + "</td>");
                    out.println("<td>");

                    // Edit icon
                    out.println("<a class='action-edit' href='home_event?event_id=" + id + "'>");
                    out.println("<i class='fa-solid fa-pen-to-square'></i></a> ");

                    // Delete form
                    out.println("<form method='post' action='home_event' style='display:inline;'>");
                    out.println("<input type='hidden' name='action' value='delete'>");
                    out.println("<input type='hidden' name='event_id' value='" + id + "'>");
                    out.println("<button type='submit' onclick='return confirm(\"Delete this event permanently?\");' style='border:none;background:none;padding:0;cursor:pointer;color:#d9534f;'>");
                    out.println("<i class='fa-solid fa-trash'></i>");
                    out.println("</button>");
                    out.println("</form>");

                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                rs.close();
                stmt.close();
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR:" + e.getMessage());
        }
    }

    /* ================= POST ================= */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // DELETE
        if ("delete".equals(action)) {
            String idParam = request.getParameter("event_id");
            if (idParam == null) return;
            try {
                int id = Integer.parseInt(idParam);
                Connection conn = DriverManager.getConnection(URL, USER, PASS);
                PreparedStatement ps = conn.prepareStatement("DELETE FROM event WHERE event_id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
                conn.close();
                response.sendRedirect("home.html?menu=Event&status=deleted");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // UPDATE
        int id = Integer.parseInt(request.getParameter("event_id"));
        String name = request.getParameter("event_name");
        String date = request.getParameter("event_date");
        int deptId = Integer.parseInt(request.getParameter("dept_id"));
        String rulebook = request.getParameter("rulebook");

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE event SET event_name=?, event_date=?, dept_id=?, rulebook=? WHERE event_id=?"
            );
            ps.setString(1, name);
            ps.setString(2, date);
            ps.setInt(3, deptId);
            ps.setString(4, rulebook);
            ps.setInt(5, id);
            ps.executeUpdate();
            ps.close();
            conn.close();
            response.sendRedirect("home.html?menu=Event");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
