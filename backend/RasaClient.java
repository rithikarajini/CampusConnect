import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RasaClient {

    private static final String RASA_URL = "http://localhost:5005/model/parse";

    public static class RasaResult {
        public String intent = "unknown";
        public String text = "";
        public Map<String, String> entities = new HashMap<>();
    }

    public static RasaResult interpret(String text) {

        RasaResult result = new RasaResult();

        try {
            URI uri = new URI(RASA_URL);
            HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String payload = "{\"text\":\"" + text + "\"}";
            con.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            // Extract intent
            if (json.has("intent")) {
                result.intent = json.getJSONObject("intent").optString("name", "unknown");
            }

            // Extract entities
            JSONArray ents = json.getJSONArray("entities");
            for (int i = 0; i < ents.length(); i++) {
                JSONObject e = ents.getJSONObject(i);
                String entity = e.getString("entity");
                String value = e.getString("value");

                result.entities.put(entity, value);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
