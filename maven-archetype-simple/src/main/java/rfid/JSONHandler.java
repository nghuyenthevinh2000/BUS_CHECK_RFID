package rfid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import rfid.FileHandler.FileDir;

public class JSONHandler {

    private JSONHandler() {
    }

    private JSONObject config_object;

    public enum JSONField {
        URL, PHYSICAL_ADDRESS_CHOOSE_POSITION, INTERNET_CONNECT_TIME_OUT_MS, READ_DATA_TIME_OUT_MS, OFFLINE_LINE
    }
    private FileDir json_file;

    public static JSONHandler getJSONHandler(FileDir json_file) {
        JSONHandler handler = new JSONHandler();
        handler.json_file = json_file;
        return handler;
    }

    public static String convertHashMapToJSONString(Map<String, String> map){
        return JSONObject.toJSONString(map);
    }

    public JSONHandler getJSONObjectFromFileConfig() {
        InputStream file = getClass().getResourceAsStream(json_file.getDir());
        InputStreamReader reader = new InputStreamReader(file);

        try {
            this.config_object = (JSONObject) new JSONParser().parse(reader);
            reader.close();
        } catch (IOException | ParseException e) {
            LogHandler.log_err(JSONHandler.class.getName(), e);
        }

        return this;
    }

    public JSONHandler getJSONObjectFromFileData() {
        InputStreamReader reader = FileHandler.getReader(json_file);

        try {
            // check if file program_state.json is empty
            if(reader.read() == -1 && json_file.equals(FileDir.PROGRAM_STATE_DIR)){
                saveArrayToFile(new String[0]);
            }

            this.config_object = (JSONObject) new JSONParser().parse(reader);
            reader.close();
        } catch (IOException | ParseException e) {
            LogHandler.log_err(JSONHandler.class.getName(), e);
        }

        return this;
    }

    public String getField(JSONField name){
        return (String)config_object.get(name.toString());
    }

    public JSONArray getArray(JSONField name){
        return (JSONArray) config_object.get(name.toString());
    }

    //save any array to json file
    public void saveArrayToFile(String[] arr){
        //figure out how to write to JSON file
        JSONObject obj = new JSONObject();
        JSONArray json_arr = new JSONArray();
        json_arr.addAll(Arrays.asList(arr));
        obj.put(JSONField.OFFLINE_LINE.name(), json_arr);
        OutputStreamWriter writer = FileHandler.getWriter(json_file, false);
        try {
            writer.write(obj.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LogHandler.log_err(JSONHandler.class.getName(), e);
        }
    }
}
