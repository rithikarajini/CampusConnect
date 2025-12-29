import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/home_exam")
public class home_exam extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/demo2?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "25swathi14";

    // -------------------- Department Mapping --------------------
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String idParam = request.getParameter("id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            // ---------------- EDIT MODE ----------------
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM exam WHERE id=?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.println("<h3>Edit Exam</h3>");
                    out.println("<form method='post' action='home_exam'>");
                    out.println("<input type='hidden' name='id' value='" + rs.getInt("id") + "'>");
                    out.println("Course Code:<input type='text' name='course_code' value='" + rs.getString("course_code") + "' required><br>");
                    out.println("Course Name:<input type='text' name='course_name' value='" + rs.getString("course_name") + "' required><br>");
                    out.println("Exam Date:<input type='date' name='exam_date' value='" + rs.getString("exam_date") + "' required><br>");
                    out.println("Classes:<input type='text' name='classes' value='" + rs.getString("classes") + "' required><br>");

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

                    out.println("Exam Type:<input type='text' name='exam_type' value='" + rs.getString("exam_type") + "' required><br>");
                    out.println("Semester:<input type='text' name='semester' value='" + rs.getString("semester") + "' required><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }

                rs.close();
                ps.close();
            } 
            // ---------------- DISPLAY TABLE ----------------
            else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM exam ORDER BY id");
                out.println("<table border='1'><tr><th>#</th><th>Course Code</th><th>Course Name</th><th>Exam Date</th><th>Classes</th><th>Department</th><th>Exam Type</th><th>Semester</th><th>Actions</th></tr>");
                int sno = 1;

                while (rs.next()) {
                    int id = rs.getInt("id");
                    out.println("<tr>");
                    out.println("<td>" + sno++ + "</td>");
                    out.println("<td>" + rs.getString("course_code") + "</td>");
                    out.println("<td>" + rs.getString("course_name") + "</td>");
                    out.println("<td>" + rs.getString("exam_date") + "</td>");
                    out.println("<td>" + rs.getString("classes") + "</td>");
                    out.println("<td>" + getDeptName(rs.getInt("dept_id")) + "</td>"); // Department Name
                    out.println("<td>" + rs.getString("exam_type") + "</td>");
                    out.println("<td>" + rs.getString("semester") + "</td>");
                    out.println("<td>");
                    out.println("<a class='action-edit' href='home_exam?id=" + id + "' style='margin-right:10px;'><i class='fa-solid fa-pen-to-square'></i></a>");

                    // Delete button with POST
                    out.println("<form method='post' action='home_exam' style='display:inline;'>");
                    out.println("<input type='hidden' name='action' value='delete'>");
                    out.println("<input type='hidden' name='id' value='" + id + "'>");
                    out.println("<button type='submit' onclick='return confirm(\"Delete this exam permanently?\");' style='border:none;background:none;padding:0;cursor:pointer;color:red;'>");
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
            out.println("ERROR: " + e.getMessage());
        }
    }

    // ---------------- POST: UPDATE / DELETE ----------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // DELETE
        if ("delete".equals(action)) {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing ID");
                return;
            }
            int id = Integer.parseInt(idParam);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM exam WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                response.sendRedirect("home.html?menu=Exam");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting exam");
            }
            return;
        }

        // UPDATE
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing ID");
            return;
        }

        int id = Integer.parseInt(idParam);
        String course_code = request.getParameter("course_code");
        String course_name = request.getParameter("course_name");
        String exam_date = request.getParameter("exam_date");
        String classes = request.getParameter("classes");
        int dept_id = Integer.parseInt(request.getParameter("dept_id"));
        String exam_type = request.getParameter("exam_type");
        String semester = request.getParameter("semester");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE exam SET course_code=?, course_name=?, exam_date=?, classes=?, dept_id=?, exam_type=?, semester=? WHERE id=?")) {

            ps.setString(1, course_code);
            ps.setString(2, course_name);
            ps.setString(3, exam_date);
            ps.setString(4, classes);
            ps.setInt(5, dept_id);
            ps.setString(6, exam_type);
            ps.setString(7, semester);
            ps.setInt(8, id);

            ps.executeUpdate();
            response.sendRedirect("home.html?menu=Exam");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
