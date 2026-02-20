import java.io.*;

import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Loginservlet")
public class Loginservlet extends HttpServlet {
    private static final String query = "INSERT INTO LOGINCC(ID,USERNAME,PASSWORD) VALUES(?,?,?)"; 
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        PrintWriter pw = response.getWriter();
        response.setContentType("text/html");
        
        // ✅ Get ID parameter for INSERT
        int id = Integer.parseInt(request.getParameter("id"));
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException cnf) {
            cnf.printStackTrace();
            pw.println("<h2>Driver Error</h2>");
            return;
        }
        
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/campusconnect?useSSL=false", "root", "Rithika@14");
            
            // ✅ INSERT new user into LOGINCC table
            PreparedStatement psInsert = conn.prepareStatement(query);
            psInsert.setInt(1, id);
            psInsert.setString(2, username);
            psInsert.setString(3, password);
            
            int insertCount = psInsert.executeUpdate();
            psInsert.close();
            
            // ✅ Check if user exists for login
            PreparedStatement psSelect = conn.prepareStatement("SELECT * FROM LOGINCC WHERE USERNAME = ? AND PASSWORD = ?");
            psSelect.setString(1, username);
            psSelect.setString(2, password);
            
            ResultSet rs = psSelect.executeQuery();
            
            if(rs.next()) {
                pw.println("<h2 style='color:green'>Login Successful!</h2>");
                pw.println("<p>ID: " + id + " | Welcome " + username + "</p>");
                pw.println("<p>Record inserted into LOGINCC table</p>");
                pw.println("<a href='chatbot.html'>Go to Chatbot</a>");
            } else {
                pw.println("<h2 style='color:red'> Login Failed</h2>");
                pw.println("<a href='logincbhtml.html'>Try Again</a>");
            }
            rs.close();
            psSelect.close();
            
        } catch(SQLException se) {
            se.printStackTrace();
            pw.println("<h2 style='color:red'>Database Error: " + se.getMessage() + "</h2>");
        } catch(Exception e) {
            e.printStackTrace();
            pw.println("<h2 style='color:red'>Error: " + e.getMessage() + "</h2>");
        } finally {
            if (conn != null) try { conn.close(); } catch(Exception e) { e.printStackTrace(); }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }
}
