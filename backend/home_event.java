import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/home_event")
public class home_event extends HttpServlet {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/campusconnect";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String idParam = request.getParameter("event_id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            if(idParam != null) {
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM event WHERE event_id=?");
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    out.println("<h3>Edit Event</h3>");
                    out.println("<form method='post' action='home_event'>");
                    out.println("<input type='hidden' name='event_id' value='"+rs.getInt("event_id")+"'>");
                    out.println("Event Name:<input type='text' name='event_name' value='"+rs.getString("event_name")+"'><br>");
                    out.println("Date:<input type='date' name='event_date' value='"+rs.getString("event_date")+"'><br>");
                    out.println("Department:<input type='text' name='department' value='"+rs.getString("department")+"'><br>");
                    out.println("Rulebook:<input type='text' name='rulebook' value='"+rs.getString("rulebook")+"'><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close();
                ps.close();
            } else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM event ORDER BY event_id");
                out.println("<table border='1'><tr><th>ID</th><th>Event Name</th><th>Date</th><th>Department</th><th>Rulebook</th><th>Actions</th></tr>");
                int i=1;
                while(rs.next()) {
                    int id = rs.getInt("event_id");
                    out.println("<tr>");
                    out.println("<td>"+i+++"</td>");
                    out.println("<td>"+rs.getString("event_name")+"</td>");
                    out.println("<td>"+rs.getString("event_date")+"</td>");
                    out.println("<td>"+rs.getString("department")+"</td>");
                    out.println("<td>"+rs.getString("rulebook")+"</td>");
                    out.println("<td>");
                    out.println("<a class='action-edit' href='home_event?event_id="+id+"'><i class='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a class='action-delete' data-id='"+id+"'><i class='fa-solid fa-trash'></i></a>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");

                out.println("<script>");
                out.println("document.querySelectorAll('a.action-delete').forEach(delLink=>{delLink.onclick=function(e){");
                out.println("e.preventDefault(); const row=this.closest('tr'); const id=this.dataset.id;");
                out.println("if(!confirm('Delete this row permanently?')) return;");
                out.println("fetch('home_event?event_id='+id,{method:'DELETE'}).then(resp=>{if(resp.ok){row.remove();} else {alert('Delete failed');}}).catch(err=>alert('Delete error:'+err));");
                out.println("}});</script>");

                rs.close();
                stmt.close();
            }

            conn.close();
        } catch(Exception e){ e.printStackTrace(); out.println("ERROR:"+e.getMessage()); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("event_id"));
        String name = request.getParameter("event_name");
        String date = request.getParameter("event_date");
        String dept = request.getParameter("department");
        String rulebook = request.getParameter("rulebook");

        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement("UPDATE event SET event_name=?, event_date=?, department=?, rulebook=? WHERE event_id=?");
            ps.setString(1,name); ps.setString(2,date); ps.setString(3,dept); ps.setString(4,rulebook); ps.setInt(5,id);
            ps.executeUpdate();
            ps.close(); conn.close();
            response.sendRedirect("add.html?menu=Event");
        } catch(Exception e){ e.printStackTrace(); }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("event_id");
        if(idParam==null){response.sendError(400,"Missing id"); return;}
        try {
            int id = Integer.parseInt(idParam);
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM event WHERE event_id=?");
            ps.setInt(1,id);
            int rows = ps.executeUpdate();
            if(rows>0) response.setStatus(200); else response.sendError(404,"Not found");
            ps.close(); conn.close();
        } catch(Exception e){e.printStackTrace(); response.sendError(500,"Error");}
    }
}
