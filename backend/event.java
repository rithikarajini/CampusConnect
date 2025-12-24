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

@WebServlet("/Event")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class event extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/campusconnect";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "25swathi14";

    private static final String UPLOAD_DIR = "uploads/rulebooks";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String eventName = request.getParameter("event_name");
        String eventDate = request.getParameter("event_date");
        String deptIdStr = request.getParameter("department");
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
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                // Insert event details with rulebook URL
                String sql = "INSERT INTO event (event_name, event_date, department, rulebook) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, eventName);
                    ps.setString(2, eventDate);
                    ps.setInt(3, deptId);
                    ps.setString(4, fileUrl);

                    int result = ps.executeUpdate();
                    if (result > 0) {
                        response.getWriter().println("Event added successfully.");
                    } else {
                        response.getWriter().println("Failed to add event.");
                    }
                }
            }
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
