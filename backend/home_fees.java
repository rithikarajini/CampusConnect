import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/home_fees")
public class home_fees extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String feesIdParam = request.getParameter("fees_id");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            /* ========= JSON MODE (EDIT) ========= */
            if (feesIdParam != null) {

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();

                PreparedStatement ps = conn.prepareStatement(
                    "SELECT fees_id, amount, last_date, semester, year FROM fees WHERE fees_id=?"
                );
                ps.setInt(1, Integer.parseInt(feesIdParam));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.print("{");
                    out.print("\"fees_id\":" + rs.getInt("fees_id") + ",");
                    out.print("\"amount\":" + rs.getInt("amount") + ",");
                    out.print("\"last_date\":\"" + rs.getDate("last_date") + "\",");
                    out.print("\"semester\":" + rs.getInt("semester") + ",");
                    out.print("\"year\":" + rs.getInt("year"));
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
                .executeQuery("SELECT fees_id, amount, last_date, semester, year FROM fees ORDER BY fees_id");

            out.println("<table>");
            out.println("<tr>");
            out.println("<th>ID</th><th>Amount</th><th>Last Date</th><th>Semester</th><th>Year</th><th>Actions</th>");
            out.println("</tr>");

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

                /* EDIT */
                out.println(
                    "<span class='edit-btn'>" +
                    "<a href='/CampusConnect/admin_panel/Fees.html?fees_id=" + fees_id + "'>" +
                    "<i class='fa-solid fa-pen-to-square' style='color:#00BFFF;'></i></a>" +
                    "</span>"
                );

                /* DELETE */
                out.println(
                    "<span class='delete-btn' " +
                    "data-type='fees' " +
                    "data-id='" + fees_id + "' " +
                    "style='cursor:pointer;color:red;'>" +
                    "<i class='fa-solid fa-trash'></i></span>"
                );

                out.println("</td>");
                out.println("</tr>");
            }
            out.println("</table>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ========= DELETE ========= */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("delete".equals(request.getParameter("action"))) {

            int feesId = Integer.parseInt(request.getParameter("fees_id"));

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM fees WHERE fees_id=?")) {

                ps.setInt(1, feesId);
                ps.executeUpdate();
            } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            response.sendRedirect("/CampusConnect/admin_panel/home.html?menu=Fees");
        }
    }
}
