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
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class event extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String DB_URL =
        "jdbc:mysql://localhost:3306/campusconnect?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Rithika@14";

    private static final String UPLOAD_BASE = "C:/uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String eventIdStr = request.getParameter("event_id"); // ⭐ KEY
        String eventName  = request.getParameter("event_name");
        String eventDate  = request.getParameter("event_date");
        int deptId        = Integer.parseInt(request.getParameter("dept"));

        /* ================= FILE UPLOAD ================= */
        Part filePart = request.getPart("rulebook");
        String filePath = null;

        if (filePart != null && filePart.getSize() > 0) {
            String fileName = new File(filePart.getSubmittedFileName()).getName();

            File dir = new File(UPLOAD_BASE);
            if (!dir.exists()) dir.mkdirs();

            File saved = new File(dir, fileName);
            filePart.write(saved.getAbsolutePath());
            filePath = saved.getAbsolutePath();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            /* ================= UPDATE ================= */
            if (eventIdStr != null && !eventIdStr.isBlank()) {

                int eventId = Integer.parseInt(eventIdStr);

                String sql =
                    "UPDATE event SET event_name=?, event_date=?, dept_id=?"
                    + (filePath != null ? ", rulebook=?" : "")
                    + " WHERE event_id=?";

                PreparedStatement ps = conn.prepareStatement(sql);

                ps.setString(1, eventName);
                ps.setString(2, eventDate);
                ps.setInt(3, deptId);

                int idx = 4;
                if (filePath != null) {
                    ps.setString(idx++, filePath);
                }
                ps.setInt(idx, eventId);

                ps.executeUpdate();
            }

            /* ================= INSERT ================= */
            else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO event (event_name, event_date, dept_id, rulebook) VALUES (?,?,?,?)"
                );
                ps.setString(1, eventName);
                ps.setString(2, eventDate);
                ps.setInt(3, deptId);
                ps.setString(4, filePath);
                ps.executeUpdate();
            }

            response.sendRedirect("./admin_panel/home.html?menu=Event");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
