package rfid;

import java.util.logging.Logger;
import java.util.Date;
import java.util.logging.Level;
import java.net.UnknownHostException;

public class TestProgram {
    private static Logger logger = Logger.getLogger(TestProgram.class.getName());
    public static void main(String[] arg) throws UnknownHostException{
        PortInputHandler input = PortInputHandler.getInputHandler();
        input.closePort();
        ServerHandler handler = ServerHandler.getServerHandler();
        int i=10;
        while(i > 1){
            String RFID_input = input.getRFIDInput();
            logger.log(Level.INFO,String.format("RFID_input = %s", RFID_input));
            String MAC_address = input.getLocalMacAddress();
            handler.postServer(RFID_input, MAC_address);
            //enable this for debug purpose
            //i--; 
        }
    }
}
