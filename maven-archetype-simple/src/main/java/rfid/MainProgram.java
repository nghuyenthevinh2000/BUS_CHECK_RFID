package rfid;

import java.util.Calendar;
import java.util.logging.Level;

import rfid.FileHandler.FileDir;
import rfid.JSONHandler.JSONField;
import rfid.csvhandler.BusCSVHandler;
import rfid.rfid_input.PortInputHandler;

public class MainProgram {
    private static ServerHandler server;
    private static BusCSVHandler bus_handler;

    public static void main(String[] arg) {
        try{
            bus_handler = BusCSVHandler.getBusCSVHandler();
            JSONHandler json = JSONHandler.getJSONHandler(FileDir.CONFIGS_DIR).getJSONObjectFromFileConfig();
            server = ServerHandler.getServerHandler().setBusHandler(bus_handler);
            PortInputHandler input = PortInputHandler.getInputHandler().closePort();
            int i = 10;
            while (i > 1) {
                checkProgramState();
                String RFID_input = input.getRFIDInput();
                //LogHandler.log_terminal(MainProgram.class.getName(), Level.INFO, RFID_input);
                if (RFID_input == null) continue;
                //LogHandler.log_terminal(MainProgram.class.getName(), Level.INFO, "has RFID_input");
                String MAC_address = input.getLocalMacAddress(Integer.parseInt(json.getField(JSONField.PHYSICAL_ADDRESS_CHOOSE_POSITION)));
                if(server.canPostServer(RFID_input))
                    server.prepareData(RFID_input, MAC_address);
                //enable this for debug purpose
                //i--;
            }
        }catch(Throwable e){
            LogHandler.log_err(MainProgram.class.getName(), e);
        }
    }

    public static void exitProgram(){
        //khi chương trình kết thúc, nhét toàn bộ offline_bus_holder vào file json
        bus_handler.saveOfflineBusHolder();

        LogHandler.log_txt(MainProgram.class.getName(), Level.INFO, "Exit Program");
        System.exit(0);
    }

    private static void checkProgramState(){
        //if pass this moment, reset txt log
        if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 0)
            LogHandler.reset_txt();

        //if internet is resumed, send all offline Bus if there is
        LogHandler.log_txt(MainProgram.class.getName(), Level.INFO, "Number of offline Bus = " + bus_handler.getOfflineBusHolderSize());
        if(server.hasInternetConectivity() && bus_handler.getOfflineBusHolderSize() > 0)
            server.postServerFromOffline();
    }
}