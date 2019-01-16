package objects;

import org.json.JSONObject;

public class ErrorGenerator {

    public String generate(int error_code, String error_reason) {
        JSONObject innerJson = new JSONObject();
        innerJson.put("error_code", error_code);
        innerJson.put("error_reason", error_reason);

        JSONObject outerJson = new JSONObject();
        outerJson.put("error", innerJson);

        String jsonString = outerJson.toString();
        System.out.println(jsonString);
        return jsonString;
    }

}
