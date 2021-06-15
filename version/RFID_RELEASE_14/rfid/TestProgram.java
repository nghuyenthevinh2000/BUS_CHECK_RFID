package rfid;

import java.util.Calendar;

import rfid.JSONHandler.JSONField;
import rfid.rfid_input.PortInputHandler;

public class TestProgram {

    public static void main(String[] arg) {
        try{
            PortInputHandler input = PortInputHandler.getInputHandler();
            input.closePort();
            ServerHandler handler = ServerHandler.getServerHandler();
            JSONHandler json = JSONHandler.getJSONHandler().getJSONObjectFromFile(JSONHandler.CONFIGS_DIR);
            int i = 10;
            while (i > 1) {
                //if pass this moment, reset txt log
                if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 0)
                    LogHandler.reset_txt();

                String RFID_input = input.getRFIDInput();
                //LogHandler.log_terminal(TestProgram.class.getName(), Level.INFO, RFID_input);
                if (RFID_input == null) continue;
                //LogHandler.log_terminal(TestProgram.class.getName(), Level.INFO, "has RFID_input");
                String MAC_address = input.getLocalMacAddress(Integer.parseInt(json.getField(JSONField.PHYSICAL_ADDRESS_CHOOSE_POSITION)));
                if(handler.canPostServer(RFID_input))
                    handler.postServer(RFID_input, MAC_address);
                //enable this for debug purpose
                //i--; 
            }
        }catch(Throwable e){
            LogHandler.log_err(TestProgram.class.getName(), e);
        }
    }
}