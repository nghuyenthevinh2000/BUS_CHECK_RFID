package rfid.csvhandler;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import rfid.FileHandler;
import rfid.FileHandler.FileDir;

public class BusCSVHandler {
    private static Logger logger = Logger.getLogger(BusCSVHandler.class.getName());

    private BusCSVHandler() {
    }

    private List<Bus> history_bus_container;
    private List<Bus> current_bus_container;
    private Map<String, Integer> bus_rfid_mapping;
    private StatefulBeanToCsv<Bus> history_bus_writer;
    private InputStreamReader history_csv_reader;
    private OutputStreamWriter history_csv_writer;


    public static BusCSVHandler getBusCSVHandler() {
        BusCSVHandler handler = new BusCSVHandler();
        handler.history_csv_reader = FileHandler.getReader(FileDir.BUS_HISTORY);
        handler.history_csv_writer = FileHandler.getWriter(FileDir.BUS_HISTORY);
        //history_bus_container chứa toàn bộ dữ liệu ra/vào của các bus
        handler.history_bus_container = new CsvToBeanBuilder<Bus>(handler.history_csv_reader).withType(Bus.class).build().parse();
        handler.current_bus_container = new LinkedList<>();
        handler.bus_rfid_mapping = new HashMap<>();
        handler.mapBusRFID_CODE();

        handler.history_bus_writer = new StatefulBeanToCsvBuilder<Bus>(handler.history_csv_writer).build();
        return handler;
    }

    public static long getNewestLine(FileDir dir) {
        long lineCount = 0;
        //find current line for set flag of offline

        return lineCount;
    }

    // Mục đích là để cho keep track số lần vào ra. Nếu như không sử dụng, có thể bỏ.
    private void mapBusRFID_CODE() {
        int index = 0;
        //
        ListIterator<Bus> listIterator = history_bus_container.listIterator(history_bus_container.size());
        //từng mã RFID sẽ được gán cho một index
        //index đó sẽ ánh xạ tới current_bus_container
        //current_bus_container chứa thông tin mới nhất của duy nhất từng mã RFID
        while(listIterator.hasPrevious()){
            Bus item = listIterator.previous();
            if(bus_rfid_mapping.containsKey(item.getRFID_CODE())) continue;
            bus_rfid_mapping.put(item.getRFID_CODE(), index);
            current_bus_container.add(item);
            index++;
        }
    }

    //lấy thông tin của Bus mới nhất theo index
    public Bus readBusByLine(int index) {
        return current_bus_container.get(index);
    }

    //lấy thông tin của Bus mới nhất theo RFID_CODE
    public Bus readBusByRFID_CODE(String RFID_CODE) {
        Bus bus_info = null;
        if (bus_rfid_mapping.containsKey(RFID_CODE)) {
            bus_info = current_bus_container.get(bus_rfid_mapping.get(RFID_CODE).intValue());
        } else {
            // no such RFID_CODE before, so I have to newly add it
            bus_info = new Bus(null, null, null);
        }

        return bus_info;
    }

    // update bus content
    public void updateBusContent(Bus bus_info) {
        //update vào file bus_history_details.csv
        try{
            history_bus_writer.write(bus_info);
            history_csv_writer.flush();
        }catch(CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e){
            e.printStackTrace();
        }

        //update thông tin mới nhất của mã RFID tương ứng
        if(bus_rfid_mapping.containsKey(bus_info.getRFID_CODE())){
            current_bus_container.set(bus_rfid_mapping.get(bus_info.getRFID_CODE()), bus_info);
        }else{
            bus_rfid_mapping.put(bus_info.getRFID_CODE(),current_bus_container.size());
            current_bus_container.add(bus_info);
        }
    }

    public void closeHistoryUpdate(){
        try{
            history_csv_writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
