package rfid.csvhandler;

import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.opencsv.bean.CsvToBeanBuilder;

import rfid.FileHandler;
import rfid.FileHandler.FileDir;

public class BusRFIDMapCSVHandler {
    private static Logger logger = Logger.getLogger(BusRFIDMapCSVHandler.class.getName());

    private BusRFIDMapCSVHandler(){}
    private Map<String, String> map;
    private InputStreamReader bus_rfid_reader;

    public static BusRFIDMapCSVHandler getBusRFIDMapCSVHandler(){
        BusRFIDMapCSVHandler handler = new BusRFIDMapCSVHandler();
        handler.bus_rfid_reader = FileHandler.getReader(FileDir.BUS_RFID);
        List<BusRFIDMap> list = new CsvToBeanBuilder<BusRFIDMap>(handler.bus_rfid_reader).withType(BusRFIDMap.class).build().parse();
        handler.map = new HashMap<>();
        for(BusRFIDMap item : list){
            handler.map.put(item.getRFID_CODE(), item.getBUS_NUMBER_PLATE());
        }

        return handler;
    }

    public String getBUS_NUMBER_PLATEforRFID_CODE(String RFID_CODE){
        return map.get(RFID_CODE);
    }

}
