package rfid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

//CHECK SOLID 
//
public class FileHandler {
    private FileHandler() {
    }

    public enum FileDir{
        BUS_HISTORY("data/bus_history_details.csv"),
        BUS_RFID("data/bus_rfid_mapping.csv"),
        LOG_OFFLINE("data/log.txt"),
        CONFIGS_DIR("config/configs.json"),
        PROGRAM_STATE_DIR("data/program_state.json");
        private final String dir;
        private FileDir(String dir){
            this.dir = dir;
        }

        public String getDir(){
            return dir;
        }
    }

    public static InputStreamReader getReader(FileDir dir) {
        FileInputStream file = null;
        try {
            file = new FileInputStream(dir.dir);
        } catch (FileNotFoundException e) {
            LogHandler.log_err(FileHandler.class.getName(), e);
        }
        return new InputStreamReader(file);
    }

    public static OutputStreamWriter getWriter(FileDir dir, boolean append){
        OutputStream output = null;
        try {
            output = new FileOutputStream(dir.dir, append);
        } catch (FileNotFoundException e) {
            LogHandler.log_err(FileHandler.class.getName(), e);
        }
        return new OutputStreamWriter(output);
    }

}
