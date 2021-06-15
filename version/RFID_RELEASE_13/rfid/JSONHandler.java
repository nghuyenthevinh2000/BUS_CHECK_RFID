package rfid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONHandler {

    private JSONHandler() {
    }

    private JSONObject config_object;

    public enum JSONField {
        URL, DEVICE_CONNECT_RETRY_TIME, DEVICE_CONNECT_FAIL_TIME_OUT_MS, PHYSICAL_ADDRESS_CHOOSE_POSITION, INTERNET_CONNECT_TIME_OUT_MS
    }
    public static final String CONFIGS_DIR = "config/configs.json";

    public static JSONHandler getJSONHandler() {
        JSONHandler handler = new JSONHandler();
        return handler;
    }

    public JSONHandler getJSONObjectFromFile(String json_file) {
        InputStreamReader reader = null;
        JSONParser jsonParser = new JSONParser();

        try {
            InputStream file = getClass().getResourceAsStream(json_file);
            reader = new InputStreamReader(file);
            Object obj = jsonParser.parse(reader);
            this.config_object = (JSONObject) obj;
        } catch (FileNotFoundException e) {
            LogHandler.log_err(JSONHandler.class.getName(), e);
        }
        catch(IOException | ParseException e){
            LogHandler.log_err(JSONHandler.class.getName(), e);
        }

        return this;
    }

    public String convertHashMapToJSONString(Map<String, String> map){
        return JSONObject.toJSONString(map);
    }

    public String getField(JSONField name){
        return (String)config_object.get(name.toString());
    }
}
