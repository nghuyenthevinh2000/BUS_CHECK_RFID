package rfid;

import java.util.logging.Logger;

import rfid.FileHandler.FileDir;
import rfid.JSONHandler.JSONField;
import rfid.rfid_input.PortInputHandler;

import java.io.OutputStreamWriter;
import java.util.logging.Level;

public class TestProgram {
    private static Logger logger = Logger.getLogger(TestProgram.class.getName());

    public static void main(String[] arg) {
        OutputStreamWriter file_handler = FileHandler.getWriter(FileDir.LOG_OFFLINE);
        PortInputHandler input = PortInputHandler.getInputHandler().setWriterForDebug(file_handler);
        input.closePort();
        ServerHandler handler = ServerHandler.getServerHandler().setWriterForDebug(file_handler);
        JSONHandler json = JSONHandler.getJSONHandler().getJSONObjectFromFile(JSONHandler.CONFIGS_DIR);
        int i = 10;
        while (i > 1) {
            String RFID_input = input.getRFIDInput();
            if (RFID_input == null) continue;
            String MAC_address = input.getLocalMacAddress(Integer.parseInt(json.getField(JSONField.PHYSICAL_ADDRESS_CHOOSE_POSITION)));
            if(handler.canPostServer(RFID_input))
                handler.postServer(RFID_input, MAC_address);
            //enable this for debug purpose
            //i--; 
        }
    }
}