import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/VoiceServlet")
public class VoiceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String userMessage = request.getParameter("message");

        PrintWriter out = response.getWriter();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            out.print("I didn't hear anything.");
            return;
        }

        // SIMPLE BOT LOGIC (test ku)
        String msg = userMessage.toLowerCase();
        String reply;

        if (msg.contains("hello") || msg.contains("hi")) {
            reply = "Hello! How can I help you?";
        } 
        else if (msg.contains("fees")) {
            reply = "You can check fee details in the Fees section.";
        }
        else if (msg.contains("exam")) {
            reply = "Exam schedules are available in the Exam module.";
        }
        else {
            reply = "You said: " + userMessage;
        }

        out.print(reply);
    }
}
