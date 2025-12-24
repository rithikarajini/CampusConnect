import java.io.*; import java.sql.*;
import jakarta.servlet.*; import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*;

@WebServlet("/home_fees")
public class home_fees extends HttpServlet {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/campusconnect";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String idParam = request.getParameter("fee_id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            if(idParam != null){
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM fees WHERE fee_id=?");
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    out.println("<h3>Edit Fee</h3>");
                    out.println("<form method='post' action='home_fees'>");
                    out.println("<input type='hidden' name='fee_id' value='"+rs.getInt("fee_id")+"'>");
                    out.println("Amount:<input type='text' name='amount' value='"+rs.getString("amount")+"'><br>");
                    out.println("Last Date:<input type='date' name='last_date' value='"+rs.getString("last_date")+"'><br>");
                    out.println("Semester:<input type='text' name='semester' value='"+rs.getString("semester")+"'><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close(); ps.close();
            } else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM fees ORDER BY fee_id");
                out.println("<table border='1'><tr><th>ID</th><th>Amount</th><th>Last Date</th><th>Semester</th><th>Actions</th></tr>");
                int i=1;
                while(rs.next()){
                    int id = rs.getInt("fee_id");
                    out.println("<tr>");
                    out.println("<td>"+i+++"</td>");
                    out.println("<td>"+rs.getString("amount")+"</td>");
                    out.println("<td>"+rs.getString("last_date")+"</td>");
                    out.println("<td>"+rs.getString("semester")+"</td>");
                    out.println("<td>");
                    out.println("<a class='action-edit' href='home_fees?fee_id="+id+"'><i class='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a class='action-delete' data-id='"+id+"'><i class='fa-solid fa-trash'></i></a>");
                    out.println("</td></tr>");
                }
                out.println("</table>");

                out.println("<script>");
                out.println("document.querySelectorAll('a.action-delete').forEach(delLink=>{delLink.onclick=function(e){");
                out.println("e.preventDefault(); const row=this.closest('tr'); const id=this.dataset.id;");
                out.println("if(!confirm('Delete this row permanently?')) return;");
                out.println("fetch('home_fees?fee_id='+id,{method:'DELETE'}).then(resp=>{if(resp.ok){row.remove();} else {alert('Delete failed');}}).catch(err=>alert('Delete error:'+err));");
                out.println("}});</script>");

                rs.close(); stmt.close();
            }

            conn.close();
        } catch(Exception e){ e.printStackTrace(); out.println("ERROR:"+e.getMessage()); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("fee_id"));
        String amount = request.getParameter("amount");
        String last_date = request.getParameter("last_date");
        String semester = request.getParameter("semester");

        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement("UPDATE fees SET amount=?, last_date=?, semester=? WHERE fee_id=?");
            ps.setString(1,amount); ps.setString(2,last_date); ps.setString(3,semester); ps.setInt(4,id);
            ps.executeUpdate(); ps.close(); conn.close();
            response.sendRedirect("add.html?menu=Fees");
        } catch(Exception e){ e.printStackTrace(); }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("fee_id");
        if(idParam==null){response.sendError(400,"Missing id"); return;}
        try{
            int id = Integer.parseInt(idParam);
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM fees WHERE fee_id=?");
            ps.setInt(1,id);
            int rows = ps.executeUpdate();
            if(rows>0) response.setStatus(200); else response.sendError(404,"Not found");
            ps.close(); conn.close();
        }catch(Exception e){e.printStackTrace(); response.sendError(500,"Error");}
    }
}
