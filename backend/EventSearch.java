

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;


@WebServlet("/EventSearch")
public class EventSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;

	    public static String answer(String text, String query) {

	        if (text == null || text.trim().isEmpty())
	            return "I cannot find details for this event. Try again later.";

	        text = text.toLowerCase();
	        query = query.toLowerCase();

	        /* REGISTRATION LINK */
	        if (query.contains("register") || query.contains("registration")) {
	            List<String> links = getUrls(text);
	            if (!links.isEmpty())
	                return "Here are the registration links:\n" + String.join("\n", links);
	        }

	        /* VENUE */
	        if (query.contains("venue") || query.contains("where")) {
	            String v = findLine(text, "venue");
	            if (v != null) return "Venue: " + v;
	        }

	        /* TIMINGS / AGENDA */
	        if (query.contains("time") || query.contains("schedule") || query.contains("agenda")) {
	            return findBlock(text, "agenda");
	        }

	        /* RULES */
	        if (query.contains("rules") || query.contains("regulation")) {
	            return findBlock(text, "rules");
	        }

	        /* CONTACTS */
	        if (query.contains("coordinator") || query.contains("contact") || query.contains("phone")) {
	            return findBlock(text, "contact");
	        }

	        return "Here is what I found:\n" + text.substring(0, Math.min(400, text.length())) + "...";
	    }

	    private static List<String> getUrls(String text) {
	        List<String> list = new ArrayList<>();
	        Matcher m = Pattern.compile("https?://\\S+").matcher(text);
	        while (m.find()) list.add(m.group());
	        return list;
	    }

	    private static String findLine(String text, String key) {
	        for (String line : text.split("\n")) {
	            if (line.contains(key)) return line;
	        }
	        return null;
	    }

	    private static String findBlock(String text, String key) {
	        String[] lines = text.split("\n");
	        StringBuilder sb = new StringBuilder();

	        boolean found = false;
	        for (String line : lines) {
	            if (line.contains(key)) {
	                found = true;
	                continue;
	            }
	            if (found) {
	                if (line.trim().isEmpty()) break;
	                sb.append(line).append("\n");
	            }
	        }

	        if (sb.length() == 0) return "No " + key + " details found";
	        return sb.toString();
	    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
