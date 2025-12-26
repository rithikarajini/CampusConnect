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
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class event extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String URL = "jdbc:mysql://localhost:3306/campusconnect?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Rithika@14";

    private static final String UPLOAD_DIR = "uploads/rulebooks";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String eventName = request.getParameter("evt_name");
        String eventDate = request.getParameter("evt_date");
        String deptIdStr = request.getParameter("dept");
        int deptId = 0;
        if (deptIdStr != null && !deptIdStr.isEmpty()) {
            deptId = Integer.parseInt(deptIdStr);
        }

        Part filePart = request.getPart("filename");
        String fileName = "";
        String fileUrl = "";

        if (filePart != null && filePart.getSize() > 0) {
            fileName = getFileName(filePart);
            String appPath = request.getServletContext().getRealPath("");
            String uploadPath = appPath + File.separator + UPLOAD_DIR;

            File uploadFolder = new File(uploadPath);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String fullFilePath = uploadPath + File.separator + fileName;
            filePart.write(fullFilePath);

            // Store relative URL (path) for the rulebook in DB
            fileUrl = UPLOAD_DIR + "/" + fileName;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                // Insert event details with rulebook URL
                String sql = "INSERT INTO event (event_name, event_date, department, rulebook) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, eventName);
                    ps.setString(2, eventDate);
                    ps.setInt(3, deptId);
                    ps.setString(4, fileUrl);
                }
            }
            response.sendRedirect("admin_panel/home.html?menu=event");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }
}
