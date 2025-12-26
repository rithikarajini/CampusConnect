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

@WebServlet("/home_faculty")
public class home_faculty extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String idParam = request.getParameter("f_id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            // EDIT FORM
            if (idParam != null) {
                int f_id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT f_id,Firstname,lastname,designation,dept_id FROM faculty WHERE f_id=?");
                ps.setInt(1, f_id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    out.println("<h3>Edit Faculty</h3>");
                    out.println("<form method='post' action='home_faculty'>");
                    out.println("<input type='hidden' name='f_id' value='"+rs.getInt("f_id")+"'>");
                    out.println("First Name:<input type='text' name='fname' value='"+rs.getString("Fisrtname")+"'><br>");
                    out.println("last Name:<input type='text' name='lname' value='"+rs.getString("lastname")+"'><br>");
                    out.println("Designation:<input type='text' name='designation' value='"+rs.getString("designation")+"'><br>");
                    out.println("dept_id:<input type='text' name='dept_id' value='"+rs.getString("dept_id")+"'><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close();
                ps.close();
            } 
            // TABLE LIST
            else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT f_id,Firstname,lastname,designation,dept_id FROM faculty ORDER BY f_id");
                out.println("<table border='1'>");
                out.println("<tr><th>f_id</th><th>First Name</th><th>last Name</th><th>Designation</th><th>dept_id</th><th>Actions</th></tr>");
                int i=1;
                while(rs.next()) {
                    int f_id = rs.getInt("f_id");
                    out.println("<tr>");
                    out.println("<td>"+i+++"</td>");
                    out.println("<td>"+rs.getString("Firstname")+"</td>");
                    out.println("<td>"+rs.getString("lastname")+"</td>");
                    out.println("<td>"+rs.getString("designation")+"</td>");
                    out.println("<td>"+rs.getString("dept_id")+"</td>");
                    out.println("<td>");
                    out.println("<a class='action-edit' href='home_faculty?f_f_id="+f_id+"'><i class='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a class='action-delete' data-f_id='"+f_id+"'><i class='fa-solid fa-trash'></i></a>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                rs.close();
                stmt.close();

                // JS for delete
                out.println("<script>");
                out.println("document.querySelectorAll('a.action-delete').forEach(delLink=>{");
                out.println("delLink.onclick=function(e){");
                out.println("e.preventDefault();");
                out.println("const row=this.closest('tr');");
                out.println("const f_id=this.dataset.f_id;");
                out.println("if(!confirm('Delete this row permanently?')) return;");
                out.println("fetch('home_faculty?f_id='+f_id,{method:'DELETE'})");
                out.println(".then(resp=>{if(resp.ok){row.remove();} else {alert('Delete failed');}})");
                out.println(".catch(err=>alert('Delete error: '+err));");
                out.println("}");
                out.println("});");
                out.println("</script>");
            }

            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
            out.println("ERROR: "+e.getMessage());
        }
    }

    // UPDATE
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int f_id = Integer.parseInt(request.getParameter("f_id"));
        String Firstname = request.getParameter("Firstname");
        String lastname = request.getParameter("lastname");
        String designation = request.getParameter("designation");
        String dept_id = request.getParameter("dept_id");

        try {
            Connection conn = DriverManager.getConnection(URL,USER, PASS);
            PreparedStatement ps = conn.prepareStatement("UPDATE faculty SET Firstname=?,lastname=?,designation=?,dept_id=? WHERE f_id=?");
            ps.setString(1, Firstname);
            ps.setString(1, lastname);
            ps.setString(2, designation);
            ps.setString(3, dept_id);
            ps.setInt(4, f_id);
            ps.executeUpdate();
            ps.close();
            conn.close();

            response.sendRedirect("home.html?menu=Faculty"); // redirect to menu page
        } catch(Exception e) { e.printStackTrace(); }
    }

    // DELETE
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("f_id");
        if(idParam==null) { response.sendError(400,"Missing f_id"); return; }
        try {
            int f_id = Integer.parseInt(idParam);
            Connection conn = DriverManager.getConnection(URL,USER,PASS);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM faculty WHERE f_id=?");
            ps.setInt(1, f_id);
            int rows = ps.executeUpdate();
            if(rows>0) response.setStatus(200);
            else response.sendError(404,"Not found");
            ps.close();
            conn.close();
        } catch(Exception e) { e.printStackTrace(); response.sendError(500,"Error"); }
    }
}
