import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/home_event")
public class home_event extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    /* ================= DEPT NAME ================= */
    private String getDeptName(int id) {
        switch (id) {
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

    /* ================= GET ================= */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String eventName = request.getParameter("event_name");
        String eventDate = request.getParameter("event_date");
        String deptIdStr = request.getParameter("dept_id");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            /* ==================================================
               AJAX MODE → RETURN JSON (NO HTML)
               ================================================== */
            if (eventName != null && eventDate != null && deptIdStr != null) {

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();

                PreparedStatement ps = conn.prepareStatement(
                    "SELECT event_id, event_name, event_date, dept_id " +
                    "FROM event WHERE event_name=? AND event_date=? AND dept_id=? LIMIT 1"
                );
                ps.setString(1, eventName);
                ps.setDate(2, Date.valueOf(eventDate));
                ps.setInt(3, Integer.parseInt(deptIdStr));

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.print("{");
                    out.print("\"event_id\":" + rs.getInt("event_id") + ",");
                    out.print("\"event_name\":\"" + rs.getString("event_name") + "\",");
                    out.print("\"event_date\":\"" + rs.getString("event_date") + "\",");
                    out.print("\"dept_id\":" + rs.getInt("dept_id"));
                    out.print("}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }

                rs.close();
                ps.close();
                return; // ⛔ DO NOT CONTINUE TO HTML
            }

            /* ==================================================
               NORMAL PAGE MODE → TABLE (UNCHANGED)
               ================================================== */
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM event ORDER BY event_id");

            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Name</th><th>Date</th><th>Dept</th><th>Action</th></tr>");

            int i = 1;
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + i++ + "</td>");
                out.println("<td>" + rs.getString("event_name") + "</td>");
                out.println("<td>" + rs.getString("event_date") + "</td>");
                out.println("<td>" + getDeptName(rs.getInt("dept_id")) + "</td>");
                out.println("<td>");
                
                String eventname = rs.getString("event_name");
                String eventdate = rs.getString("event_date");
                int deptId = rs.getInt("dept_id");

				out.println("<span class='edit-btn'>");
				out.println(
				    "<a href='/CampusConnect/admin_panel/Event.html" +
				    "?event_name=" + eventname+
				    "&event_date=" + eventdate +
				    "&dept_id=" + deptId + "'>"
				);
				out.println("<i class='fa-solid fa-pen-to-square' style='color:#00BFFF;'></i></a>");
				out.println("</span>");

                // DELETE ICON (UNCHANGED)
				out.println(
						  "<span class='delete-btn' " +
						  "data-type='event' " +
						  "data-id='" + rs.getInt("event_id") + "' " +
						  "style='cursor:pointer;color:red;'>"
						);
						out.println("<i class='fa-solid fa-trash'></i>");
						out.println("</span>");


                out.println("</td>");
                out.println("</tr>");
            }
            out.println("</table>");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("ERROR");
        }
    }

    /* ================= POST ================= */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            /* ---------- DELETE (UNCHANGED) ---------- */
            if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("event_id"));
                PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM event WHERE event_id=?"
                );
                ps.setInt(1, id);
                ps.executeUpdate();
                response.sendRedirect("admin_panel/home.html?menu=Event");
                return;
            }

            /* ---------- UPDATE (LOGIC ONLY, NO ID DEPENDENCY) ---------- */
            if ("update".equals(action)) {
                String eventName = request.getParameter("event_name");
                String eventDate = request.getParameter("event_date");
                String deptId    = request.getParameter("dept_id");

                PreparedStatement ps = conn.prepareStatement(
                    "SELECT event_id FROM event WHERE event_name=? AND event_date=? AND dept_id=? LIMIT 1"
                );
                ps.setString(1, eventName);
                ps.setDate(2, Date.valueOf(eventDate));
                ps.setInt(3, Integer.parseInt(deptId));

                ps.executeQuery(); // just lookup
                response.sendRedirect("admin_panel/home.html?menu=Event");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
