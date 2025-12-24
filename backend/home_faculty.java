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

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/campusconnect";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String idParam = request.getParameter("id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            // EDIT FORM
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT id,name,designation,department FROM faculty WHERE id=?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    out.println("<h3>Edit Faculty</h3>");
                    out.println("<form method='post' action='home_faculty'>");
                    out.println("<input type='hidden' name='id' value='"+rs.getInt("id")+"'>");
                    out.println("Name:<input type='text' name='name' value='"+rs.getString("name")+"'><br>");
                    out.println("Designation:<input type='text' name='designation' value='"+rs.getString("designation")+"'><br>");
                    out.println("Department:<input type='text' name='department' value='"+rs.getString("department")+"'><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                }
                rs.close();
                ps.close();
            } 
            // TABLE LIST
            else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id,name,designation,department FROM faculty ORDER BY id");
                out.println("<table border='1'>");
                out.println("<tr><th>ID</th><th>Name</th><th>Designation</th><th>Department</th><th>Actions</th></tr>");
                int i=1;
                while(rs.next()) {
                    int id = rs.getInt("id");
                    out.println("<tr>");
                    out.println("<td>"+i+++"</td>");
                    out.println("<td>"+rs.getString("name")+"</td>");
                    out.println("<td>"+rs.getString("designation")+"</td>");
                    out.println("<td>"+rs.getString("department")+"</td>");
                    out.println("<td>");
                    out.println("<a class='action-edit' href='home_faculty?id="+id+"'><i class='fa-solid fa-pen-to-square'></i></a> ");
                    out.println("<a class='action-delete' data-id='"+id+"'><i class='fa-solid fa-trash'></i></a>");
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
                out.println("const id=this.dataset.id;");
                out.println("if(!confirm('Delete this row permanently?')) return;");
                out.println("fetch('home_faculty?id='+id,{method:'DELETE'})");
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

        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String designation = request.getParameter("designation");
        String department = request.getParameter("department");

        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement("UPDATE faculty SET name=?,designation=?,department=? WHERE id=?");
            ps.setString(1, name);
            ps.setString(2, designation);
            ps.setString(3, department);
            ps.setInt(4, id);
            ps.executeUpdate();
            ps.close();
            conn.close();

            response.sendRedirect("add.html?menu=Faculty"); // redirect to menu page
        } catch(Exception e) { e.printStackTrace(); }
    }

    // DELETE
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if(idParam==null) { response.sendError(400,"Missing id"); return; }
        try {
            int id = Integer.parseInt(idParam);
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM faculty WHERE id=?");
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if(rows>0) response.setStatus(200);
            else response.sendError(404,"Not found");
            ps.close();
            conn.close();
        } catch(Exception e) { e.printStackTrace(); response.sendError(500,"Error"); }
    }
}
