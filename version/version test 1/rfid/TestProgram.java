package rfid;

import java.util.logging.Logger;

import rfid.FileHandler.FileDir;
import rfid.JSONHandler.JSONField;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class TestProgram {
    private static Logger logger = Logger.getLogger(TestProgram.class.getName());

    public static void main(String[] arg) {
        PortInputHandler input = PortInputHandler.getInputHandler();
        input.closePort();
        ServerHandler handler = ServerHandler.getServerHandler();
        JSONHandler json = JSONHandler.getJSONHandler().getJSONObjectFromFile(ServerHandler.CONFIGS_DIR);
        OutputStreamWriter file_handler = FileHandler.getWriter(FileDir.LOG_OFFLINE);
        int i = 10;
        while (i > 1) {
            String RFID_input = input.getRFIDInput();
            if (RFID_input == null) continue;
            String MAC_address = input.getLocalMacAddress(Integer.parseInt(json.getField(JSONField.PHYSICAL_ADDRESS_CHOOSE_POSITION)));
            String date_log = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            try {
                file_handler.append(String.format("Time = %s, RFID_input = %s, MAC_address = %s", date_log, RFID_input, MAC_address));
                file_handler.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //logger.log(Level.INFO,String.format("RFID_input = %s, MAC_address = %s", RFID_input, MAC_address)); 
            //handler.postServer(RFID_input, MAC_address);
            //enable this for debug purpose
            //i--; 
        }
    }
}