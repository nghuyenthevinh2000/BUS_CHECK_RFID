package rfid.csvhandler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import org.json.simple.JSONArray;

import rfid.FileHandler;
import rfid.JSONHandler;
import rfid.LogHandler;
import rfid.FileHandler.FileDir;
import rfid.JSONHandler.JSONField;

public class BusCSVHandler {

    public class BusHolder{
        private Bus bus;
        private int line;

        public BusHolder(Bus bus, int line){
            this.bus = bus;
            this.line = line;
        }

        public Bus getBus(){
            return bus;
        }
    }

    private BusCSVHandler() {
    }

    private StatefulBeanToCsv<Bus> history_bus_writer;
    private OutputStreamWriter history_csv_writer;
    private LinkedList<BusHolder> offline_bus_holder;
    private JSONHandler states;
    private int currLine;

    public static BusCSVHandler getBusCSVHandler() {
        BusCSVHandler handler = new BusCSVHandler();
        handler.history_csv_writer = FileHandler.getWriter(FileDir.BUS_HISTORY, true);
        handler.initializeBusHistory();

        handler.history_bus_writer = new StatefulBeanToCsvBuilder<Bus>(handler.history_csv_writer).build();
        return handler;
    }

    //update thông tin của Bus nếu không có internet
    public synchronized void updateOfflineBusHolder(BusHolder bus_holder){
        offline_bus_holder.push(bus_holder);
    }

    //lấy Bus tiếp theo
    public synchronized BusHolder getOfflineBusHolderNext(){
        if(offline_bus_holder.isEmpty())
            return null;
        
        return offline_bus_holder.pop();
    }

    //kiểm tra size của offline_bus_holder
    public synchronized int getOfflineBusHolderSize(){
        return offline_bus_holder.size();
    }

    //lưu offline_bus_holder
    public synchronized void saveOfflineBusHolder(){
        String lines[] = new String[offline_bus_holder.size()];
        int i=offline_bus_holder.size()-1;
        for(BusHolder holder : offline_bus_holder){
            lines[i--] = Integer.toString(holder.line);
        }

        states.saveArrayToFile(lines, false);
    }

    // update bus content
    public void updateBusContent(Bus bus_info) {
        //update vào file bus_history_details.csv
        try{
            history_bus_writer.write(bus_info); 
            history_csv_writer.flush();
            currLine++;
        }catch(CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e){
            LogHandler.log_err(BusCSVHandler.class.getName(), e);
        }
    }

    //lấy line mới nhất của bus_history_details.csv
    public int getCurrLine(){
        return currLine;
    }

    //update state of bus_history_details.csv
    private void initializeBusHistory(){
        //get state of program from program_state.json
        states = JSONHandler.getJSONHandler(FileDir.PROGRAM_STATE_DIR).getJSONObjectFromFileData();
        JSONArray arr = states.getArray(JSONField.OFFLINE_LINE);
        List<Bus> bus_list = new CsvToBeanBuilder<Bus>(FileHandler.getReader(FileDir.BUS_HISTORY)).withType(Bus.class).build().parse();
        currLine = bus_list.size()-1;

        //from line_semaphore, reconstruct offline_bus_holder
        offline_bus_holder = new LinkedList<>();

        //if someone decide to erase all bus_history_details.csv and there is offline bus to push, start new one.
        if(bus_list.isEmpty() && !arr.isEmpty())
            return;

        //get line_semaphore to determine which line needed to offline send
        for(int i=arr.size()-1;i>=0;i--){
            int index = Integer.parseInt((String) arr.get(i));
            offline_bus_holder.push(new BusHolder(bus_list.get(index), index));
        }

        //write blank into program_state.json for stability
        states.saveArrayToFile(new String[0], false);
    }

    public void demandCloseWriter(){
        try {
            history_csv_writer.close();
        } catch (IOException e) {
            LogHandler.log_err(BusCSVHandler.class.getName(), e);
        }
    }
}
