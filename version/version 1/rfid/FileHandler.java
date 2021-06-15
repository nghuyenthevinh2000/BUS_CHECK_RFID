package rfid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;
import java.util.logging.Level;

//CHECK SOLID 
//
public class FileHandler {
    private FileHandler() {
    }

    public enum FileDir{
        BUS_HISTORY("data/bus_history_details.csv"),
        BUS_RFID("data/bus_rfid_mapping.csv");
        public final String dir;
        private FileDir(String dir){
            this.dir = dir;
        }
    }
    private static Logger logger = Logger.getLogger(FileHandler.class.getName());

    public static InputStreamReader getReader(FileDir dir) {
        FileInputStream file = null;
        try {
            file = new FileInputStream(dir.dir);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader reader = new InputStreamReader(file);

        return reader;
    }

    public static OutputStreamWriter getWriter(FileDir dir){
        OutputStream output = null;
        try {
            output = new FileOutputStream(dir.dir, true);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        OutputStreamWriter writer = new OutputStreamWriter(output);

        return writer;
    }

}
