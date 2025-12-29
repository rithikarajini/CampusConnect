import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/event")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,   // 2MB
    maxFileSize = 1024 * 1024 * 10,        // 10MB
    maxRequestSize = 1024 * 1024 * 50      // 50MB
)
public class event extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String DB_URL =
        "jdbc:mysql://localhost:3306/demo2?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "25swathi14";

    private static final String UPLOAD_DIR = "uploads/rulebooks";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 🔹 Get form values
        String eventName = request.getParameter("evt_name");
        String eventDate = request.getParameter("evt_date");
        String deptStr   = request.getParameter("dept");

        int deptId = 0;
        if(deptStr != null && !deptStr.isEmpty()) {
            deptId = Integer.parseInt(deptStr); // integer value to match DB
        }

        // 🔹 File upload
        Part filePart = request.getPart("filename");
        String fileUrl = "";

        if (filePart != null && filePart.getSize() > 0) {

            String fileName = extractFileName(filePart);

            String appPath = request.getServletContext().getRealPath("");
            String uploadPath = appPath + File.separator + UPLOAD_DIR;

            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String fullPath = uploadPath + File.separator + fileName;
            filePart.write(fullPath);

            fileUrl = UPLOAD_DIR + "/" + fileName;
        }

        // 🔹 DB INSERT
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASS
            );

            String sql =
              "INSERT INTO `event` (event_name, event_date, dept_id, rulebook) " +
              "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            ps.setString(2, eventDate);
            ps.setInt(3, deptId);      // ✅ integer
            ps.setString(4, fileUrl);

            int rows = ps.executeUpdate();   // ⭐ VERY IMPORTANT ⭐

            ps.close();
            conn.close();

            if (rows > 0) {
                response.sendRedirect("home.html?menu=event&status=success");
            } else {
                response.getWriter().println("Insert Failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    // 🔹 File name extract helper
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2,
                        token.length() - 1);
            }
        }
        return "";
    }
}
