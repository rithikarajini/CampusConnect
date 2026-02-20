import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@WebServlet("/launch")
public class LaunchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Always respond quickly to ESP32
        resp.setContentType("text/plain");
        resp.getWriter().write("LAUNCH_OK");

        // Launch chatbot UI on server machine
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(
                    new URI("http://localhost:8080/CampusConnect/chatbot/chatbot.html")
                );
            }
        } catch (Exception e) {
            e.printStackTrace(); // visible in Eclipse console
        }
    }
}

