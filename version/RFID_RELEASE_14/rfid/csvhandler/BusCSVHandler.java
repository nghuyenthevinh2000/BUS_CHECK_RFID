package rfid.csvhandler;

import java.util.List;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import rfid.FileHandler;
import rfid.LogHandler;
import rfid.FileHandler.FileDir;

public class BusCSVHandler {

    private BusCSVHandler() {
    }

    private List<Bus> history_bus_container;
    private StatefulBeanToCsv<Bus> history_bus_writer;
    private InputStreamReader history_csv_reader;
    private OutputStreamWriter history_csv_writer;


    public static BusCSVHandler getBusCSVHandler() {
        BusCSVHandler handler = new BusCSVHandler();
        //handler.history_csv_reader = FileHandler.getReader(FileDir.BUS_HISTORY);
        handler.history_csv_writer = FileHandler.getWriter(FileDir.BUS_HISTORY);
        //history_bus_container chứa toàn bộ dữ liệu ra/vào của các bus
        //handler.history_bus_container = new CsvToBeanBuilder<Bus>(handler.history_csv_reader).withType(Bus.class).build().parse();

        handler.history_bus_writer = new StatefulBeanToCsvBuilder<Bus>(handler.history_csv_writer).build();
        return handler;
    }

    public static long getNewestLine(FileDir dir) {
        long lineCount = 0;
        //find current line for set flag of offline

        return lineCount;
    }

    //lấy thông tin của Bus mới nhất theo RFID_CODE

    // update bus content
    public void updateBusContent(Bus bus_info) {
        //update vào file bus_history_details.csv
        try{
            history_bus_writer.write(bus_info); 
            history_csv_writer.flush();
        }catch(CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e){
            LogHandler.log_err(BusCSVHandler.class.getName(), e);
        }
    }
}
