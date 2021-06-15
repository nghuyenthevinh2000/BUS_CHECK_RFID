package rfid.csvhandler;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.opencsv.bean.CsvToBeanBuilder;

import rfid.FileHandler;
import rfid.FileHandler.FileDir;

public class BusRFIDMapCSVHandler {

    private BusRFIDMapCSVHandler(){}
    private List<String> map;
    private InputStreamReader bus_rfid_reader;

    public static BusRFIDMapCSVHandler getBusRFIDMapCSVHandler(){
        BusRFIDMapCSVHandler handler = new BusRFIDMapCSVHandler();
        handler.bus_rfid_reader = FileHandler.getReader(FileDir.BUS_RFID);
        List<BusRFIDMap> list = new CsvToBeanBuilder<BusRFIDMap>(handler.bus_rfid_reader).withType(BusRFIDMap.class).build().parse();
        handler.map = new LinkedList<>();
        for(BusRFIDMap item : list){
            handler.map.add(item.getRFID_CODE());
        }

        return handler;
    }

    public boolean hasRFID_INPUT(String RFID_INPUT){
        if(RFID_INPUT.contains("170-180-30032117103232")) 
            return true;

        return false;
    }
}
