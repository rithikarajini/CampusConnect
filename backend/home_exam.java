import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/home_exam")
public class home_exam extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    /* ================= DEPT NAME ================= */
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

    /* ================= GET ================= */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String courseCode = request.getParameter("course_code");
        String examDate   = request.getParameter("exam_date");
        String deptIdStr  = request.getParameter("dept_id");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            /* ========== AJAX MODE (FOR EDIT PAGE) ========== */
            if (courseCode != null && examDate != null && deptIdStr != null) {

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();

                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM exam WHERE course_code=? AND exam_date=? AND dept_id=? LIMIT 1"
                );
                ps.setString(1, courseCode);
                ps.setDate(2, Date.valueOf(examDate));
                ps.setInt(3, Integer.parseInt(deptIdStr));

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.print("{");
                    out.print("\"course_code\":\"" + rs.getString("course_code") + "\",");
                    out.print("\"course_name\":\"" + rs.getString("course_name") + "\",");
                    out.print("\"exam_date\":\"" + rs.getString("exam_date") + "\",");
                    out.print("\"classes\":\"" + rs.getInt("classes") + "\",");
                    out.print("\"dept_id\":" + rs.getInt("dept_id") + ",");
                    out.print("\"exam_type\":\"" + rs.getString("exam_type") + "\",");
                    out.print("\"semester\":\"" + rs.getString("semester") + "\"");
                    out.print("}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            /* ========== NORMAL TABLE VIEW ========== */
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM exam ORDER BY exam_date");

            out.println("<table border='1'>");
            out.println("<tr>");
            out.println("<th>ID</th>");
            out.println("<th>Course Code</th>");
            out.println("<th>Course Name</th>");
            out.println("<th>Exam Date</th>");
            out.println("<th>Department</th>");
            out.println("<th>Actions</th>");
            out.println("</tr>");

            int i = 1;
            while (rs.next()) {
                String code = rs.getString("course_code");
                String date = rs.getString("exam_date");
                int deptId  = rs.getInt("dept_id");

                out.println("<tr>");
                out.println("<td>" + i++ + "</td>");
                out.println("<td>" + code + "</td>");
                out.println("<td>" + rs.getString("course_name") + "</td>");
                out.println("<td>" + date + "</td>");
                out.println("<td>" + getDeptName(deptId) + "</td>");
                out.println("<td>");

                /* ===== EDIT ICON ===== */
                out.println("<span class='edit-btn'>");
                out.println(
                    "<a href='/CampusConnect/admin_panel/Exam.html" +
                    "?course_code=" + code +
                    "&exam_date=" + date +
                    "&dept_id=" + deptId + "'>"
                );
                out.println("<i class='fa-solid fa-pen-to-square' style='color:#00BFFF;'></i></a>");
                out.println("</span>");

                /* ===== DELETE ICON ===== */
                out.println(
                		  "<span class='delete-btn' " +
                		  "data-type='exam' " +
                		  "data-course='" + code + "' " +
                		  "data-date='" + date + "' " +
                		  "data-dept='" + deptId + "' " +
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

    /* ================= POST (DELETE ONLY) ================= */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("delete".equals(action)) {

            String code = request.getParameter("course_code");
            String date = request.getParameter("exam_date");
            int deptId  = Integer.parseInt(request.getParameter("dept_id"));

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM exam WHERE course_code=? AND exam_date=? AND dept_id=?"
                 )) {

                ps.setString(1, code);
                ps.setDate(2, Date.valueOf(date));
                ps.setInt(3, deptId);
                ps.executeUpdate();

                response.sendRedirect("./admin_panel/home.html?menu=Exam");

            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException(e);
            }
        }
    }
}
