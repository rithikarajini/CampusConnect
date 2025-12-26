import java.io.*; import java.sql.*;
import jakarta.servlet.*; import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*;

@WebServlet("/home_exam")
public class home_exam extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String idParam = request.getParameter("id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL,USER, PASS);

            if(idParam != null){
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM exam WHERE id=?");
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    out.println("<h3>Edit Exam</h3>");
                    out.println("<form method='post' action='home_exam'>");
                    out.println("<input type='hidden' name='id' value='"+rs.getInt("id")+"'>");
                    out.println("Course Code:<input type='text' name='course_code' value='"+rs.getString("course_code")+"'><br>");
                    out.println("Course Name:<input type='text' name='course_name' value='"+rs.getString("course_name")+"'><br>");
                    out.println("Exam Date:<input type='date' name='exam_date' value='"+rs.getString("exam_date")+"'><br>");
                    out.println("classes:<input type='text' name='classes' value='"+rs.getString("classes")+"'><br>");
                    out.println("Dept Id:<input type='text' name='dept_id' value='"+rs.getString("dept_id")+"'><br>");
                    out.println("Exam Type:<input type='text' name='exam_type' value='"+rs.getString("exam_type")+"'><br>");
                    out.println("semester:<input type='text' name='semester' value='"+rs.getString("semester")+"'><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close(); ps.close();
            } else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM exam ORDER BY id");
                out.println("<table border='1'><tr><th>ID</th><th>Course Code</th><th>Course Name</th><th>Exam Date</th><th>classes</th><th>Dept Id</th><th>Exam Type</th><th>semester</th><th>Actions</th></tr>");
                int i=1;
                while(rs.next()){
                    int id = rs.getInt("id");
                    out.println("<tr>");
                    out.println("<td>"+i+++"</td>");
                    out.println("<td>"+rs.getString("course_code")+"</td>");
                    out.println("<td>"+rs.getString("course_name")+"</td>");
                    out.println("<td>"+rs.getString("exam_date")+"</td>");
                    out.println("<td>"+rs.getString("classes")+"</td>");
                    out.println("<td>"+rs.getString("dept_id")+"</td>");
                    out.println("<td>"+rs.getString("exam_type")+"</td>");
                    out.println("<td>"+rs.getString("semester")+"</td>");
                    out.println("<td>");
                    out.println("<a classes='action-edit' href='home_exam?id="+id+"'><i classes='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a classes='action-delete' data-id='"+id+"'><i classes='fa-solid fa-trash'></i></a>");
                    out.println("</td></tr>");
                }
                out.println("</table>");

                out.println("<script>");
                out.println("document.querySelectorAll('a.action-delete').forEach(delLink=>{delLink.onclick=function(e){");
                out.println("e.preventDefault(); const row=this.closest('tr'); const id=this.dataset.id;");
                out.println("if(!confirm('Delete this row permanently?')) return;");
                out.println("fetch('home_exam?id='+id,{method:'DELETE'}).then(resp=>{if(resp.ok){row.remove();} else {alert('Delete failed');}}).catch(err=>alert('Delete error:'+err));");
                out.println("}});</script>");

                rs.close(); stmt.close();
            }

            conn.close();
        } catch(Exception e){ e.printStackTrace(); out.println("ERROR:"+e.getMessage()); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String course_code = request.getParameter("course_code");
        String course_name = request.getParameter("course_name");
        String exam_date = request.getParameter("exam_date");
        String cls = request.getParameter("classes");
        String dept_id = request.getParameter("dept_id");
        String exam_type = request.getParameter("exam_type");
        String semester = request.getParameter("semester");

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement ps = conn.prepareStatement("UPDATE exam SET course_code=?, course_name=?, exam_date=?, classes=?, dept_id=?, exam_type=?, semester=? WHERE id=?");
            ps.setString(1,course_code); ps.setString(2,course_name); ps.setString(3,exam_date);
            ps.setString(4,cls); ps.setString(5,dept_id); ps.setString(6,exam_type); ps.setInt(7,id);ps.setString(8,semester);
            ps.executeUpdate();
            ps.close(); conn.close();
            response.sendRedirect("home.html?menu=Exam");
        } catch(Exception e){ e.printStackTrace(); }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if(idParam==null){response.sendError(400,"Missing id"); return;}
        try{
            int id = Integer.parseInt(idParam);
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM exam WHERE id=?");
            ps.setInt(1,id);
            int rows = ps.executeUpdate();
            if(rows>0) response.setStatus(200); else response.sendError(404,"Not found");
            ps.close(); conn.close();
        }catch(Exception e){e.printStackTrace(); response.sendError(500,"Error");}
    }
}
