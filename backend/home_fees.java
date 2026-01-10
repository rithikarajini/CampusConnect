import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home_fees")
public class home_fees extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String feesIdParam = request.getParameter("fees_id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            // ---------------- EDIT FORM ----------------
            if (feesIdParam != null) {
                int fees_id = Integer.parseInt(feesIdParam);
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT fees_id, amount, last_date, semester, year FROM fees WHERE fees_id=?");
                ps.setInt(1, fees_id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    out.println("<h3>Edit Fee</h3>");
                    out.println("<form method='post' action='home_fees'>");
                    out.println("<input type='hidden' name='fees_id' value='" + rs.getInt("fees_id") + "'>");
                    out.println("Amount:<input type='number' name='amount' value='" + rs.getInt("amount") + "' required><br>");
                    out.println("Last Date:<input type='date' name='last_date' value='" + rs.getDate("last_date") + "' required><br>");
                    out.println("Semester:<input type='number' name='semester' value='" + rs.getInt("semester") + "' required><br>");
                    out.println("Year:<input type='number' name='year' value='" + rs.getInt("year") + "' required><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close();
                ps.close();
            } 
            // ---------------- TABLE LIST ----------------
            else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT fees_id, amount, last_date, semester, year FROM fees ORDER BY fees_id");

                out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");
                out.println("<table>");
                out.println("<tr><th>#</th><th>Amount</th><th>Last Date</th><th>Semester</th><th>Year</th><th>Actions</th></tr>");
                int i = 1;
                while (rs.next()) {
                    int fees_id = rs.getInt("fees_id");
                    out.println("<tr>");
                    out.println("<td>" + i++ + "</td>");
                    out.println("<td>" + rs.getInt("amount") + "</td>");
                    out.println("<td>" + rs.getDate("last_date") + "</td>");
                    out.println("<td>" + rs.getInt("semester") + "</td>");
                    out.println("<td>" + rs.getInt("year") + "</td>");
                    out.println("<td>");
                    out.println("<a class='action-edit' href='home_fees?fees_id=" + fees_id + "'><i class='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a class='action-delete' data-fees_id='" + fees_id + "'><i class='fa-solid fa-trash'></i></a>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                out.println("</div>");
                rs.close();
                stmt.close();

                // JS for delete
                out.println("<script>");
                out.println("document.querySelectorAll('a.action-delete').forEach(delLink => {");
                out.println("  delLink.onclick = function(e) {");
                out.println("    e.preventDefault();");
                out.println("    const fees_id = this.dataset.fees_id;");
                out.println("    if(!confirm('Delete this fee permanently?')) return;");
                out.println("    const form = document.createElement('form');");
                out.println("    form.method = 'post';");
                out.println("    form.action = 'home_fees';");
                out.println("    const inputAction = document.createElement('input');");
                out.println("    inputAction.type = 'hidden'; inputAction.name = 'action'; inputAction.value = 'delete'; form.appendChild(inputAction);");
                out.println("    const inputId = document.createElement('input');");
                out.println("    inputId.type = 'hidden'; inputId.name = 'fees_id'; inputId.value = fees_id; form.appendChild(inputId);");
                out.println("    document.body.appendChild(form); form.submit();");
                out.println("  }");
                out.println("});");
                out.println("</script>");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR: " + e.getMessage());
        }
    }

    // ---------------- ADD / UPDATE / DELETE ----------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // ---------------- DELETE ----------------
        if ("delete".equals(action)) {
            String feesIdParam = request.getParameter("fees_id");
            if (feesIdParam == null || feesIdParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Fees ID");
                return;
            }

            int fees_id = Integer.parseInt(feesIdParam);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM fees WHERE fees_id=?")) {
                ps.setInt(1, fees_id);
                ps.executeUpdate();
                response.sendRedirect("home.html?menu=Fees");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting fee");
            }
            return;
        }

        // ---------------- ADD / UPDATE ----------------
        String feesIdParam = request.getParameter("fees_id");
        String amountStr = request.getParameter("amount");
        String lastDateStr = request.getParameter("last_date");
        String semesterStr = request.getParameter("semester");
        String yearStr = request.getParameter("year");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            if (feesIdParam != null && !feesIdParam.isEmpty()) {
                // UPDATE
                int fees_id = Integer.parseInt(feesIdParam);
                String sql = "UPDATE fees SET amount=?, last_date=?, semester=?, year=? WHERE fees_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(amountStr));
                ps.setDate(2, java.sql.Date.valueOf(lastDateStr));
                ps.setInt(3, Integer.parseInt(semesterStr));
                ps.setInt(4, Integer.parseInt(yearStr));
                ps.setInt(5, fees_id);
                ps.executeUpdate();
            } else {
                // INSERT
                String sql = "INSERT INTO fees (amount, last_date, semester, year) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(amountStr));
                ps.setDate(2, java.sql.Date.valueOf(lastDateStr));
                ps.setInt(3, Integer.parseInt(semesterStr));
                ps.setInt(4, Integer.parseInt(yearStr));
                ps.executeUpdate();
            }

            response.sendRedirect("home.html?menu=Fees");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error processing fee");
        }
    }
}
