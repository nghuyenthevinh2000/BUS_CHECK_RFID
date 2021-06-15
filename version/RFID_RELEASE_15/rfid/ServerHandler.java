package rfid;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;

import rfid.FileHandler.FileDir;
import rfid.JSONHandler.JSONField;
import rfid.csvhandler.Bus;
import rfid.csvhandler.BusCSVHandler;
import rfid.csvhandler.BusRFIDMapCSVHandler;

public class ServerHandler {
    private ServerHandler() {
    }

    private JSONHandler config_handler;
    private BusCSVHandler csv_handler;
    private BusRFIDMapCSVHandler bus_rfid_handler;
    private String previousRFID = "";
    private Thread post_server_offline;

    class ServerOffline implements Runnable{

        @Override
        public void run(){
            BusCSVHandler.BusHolder next = csv_handler.getOfflineBusHolderNext();
            while(next != null){
                //if post fail, add it to offline_bus_holder
                if(!pushServer(next.getBus()))
                    csv_handler.updateOfflineBusHolder(next);

                next = csv_handler.getOfflineBusHolderNext();
            }
        }

    }

    public static ServerHandler getServerHandler() {
        ServerHandler server_handler = new ServerHandler();
        server_handler.config_handler = JSONHandler.getJSONHandler(FileDir.CONFIGS_DIR).getJSONObjectFromFileConfig();
        server_handler.bus_rfid_handler = BusRFIDMapCSVHandler.getBusRFIDMapCSVHandler();
        server_handler.post_server_offline = new Thread(server_handler.new ServerOffline());

        return server_handler;
    }

    public ServerHandler setBusHandler(BusCSVHandler csv_handler){
        this.csv_handler = csv_handler;

        return this;
    }

    public boolean canPostServer(String RFID_INPUT){
        //if RFID_INPUT is not in bus_rfid_mapping, return false
        if(!bus_rfid_handler.hasRFID_INPUT(RFID_INPUT)){
            return false;
        }
        
        //if RFID_INPUT is in bus_rfid_mapping, does it exist before?
        if(!RFID_INPUT.equals(previousRFID) ){
            previousRFID = RFID_INPUT;
            return true;
        }

        return false;
    }

    public void prepareData(String RFID_input, String MAC_address) {
        // prepare data into correct format
        Bus bus_info = processInputToBus(RFID_input, MAC_address);
        // update log offline in case there is no network
        csv_handler.updateBusContent(bus_info);
        // write to LOG_OFFLINE for debug purpose
        String log_content = String.format("DATE_TIME = %s, RFID_input = %s, MAC_address = %s", bus_info.getDATE_TIME(), RFID_input, MAC_address);
        LogHandler.log_txt(ServerHandler.class.getName(), Level.INFO, log_content);

        //check connection to server
        if(!hasInternetConectivity()){
            LogHandler.log_txt(ServerHandler.class.getName(), Level.SEVERE, "No internet connection");
            csv_handler.updateOfflineBusHolder(csv_handler.new BusHolder(bus_info, csv_handler.getCurrLine()));
            return;
        }

        // push to server, if post fails, add to offline_bus_holder
        if(!pushServer(bus_info))
            csv_handler.updateOfflineBusHolder(csv_handler.new BusHolder(bus_info, csv_handler.getCurrLine()));
    }

    public void postServerFromOffline(){
        if(post_server_offline.getState() == State.TERMINATED || post_server_offline.getState() == State.NEW)
            post_server_offline.start();
    }

    public boolean hasInternetConectivity() {
        try {
            URL url = new URL(config_handler.getField(JSONField.URL));
            URLConnection connection = url.openConnection();
            connection.connect();
        } catch (IOException e) {
            return false;
        }
        
        return true;
    }

    private Bus processInputToBus(String RFID_input, String MAC_address) {
        String DATE_TIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

        return new Bus(RFID_input, DATE_TIME, MAC_address);
    }

    //push Bus to server
    private boolean pushServer(Bus bus_info){
        // prepare data
        HashMap<String, String> map_value = new HashMap<String, String>() {
            private static final long serialVersionUID = -6185188228197884639L;
        };
        map_value.put("DATE_TIME", bus_info.getDATE_TIME());
        map_value.put("RFID_CODE", bus_info.getRFID_CODE());
        map_value.put("MAC_ADDRESS", bus_info.getMAC_ADDRESS());

        // push data to server
        String requestBody = JSONHandler.convertHashMapToJSONString(map_value);
        LogHandler.log_txt(ServerHandler.class.getName(),Level.INFO, requestBody);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.config_handler.getField(JSONField.URL)))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            // MEGUMIN: further change to here
            LogHandler.log_err(ServerHandler.class.getName(), e);
            Thread.currentThread().interrupt();
        }
        
        if (response != null){
            LogHandler.log_txt(ServerHandler.class.getName(),Level.INFO, response.body());
            return true;
        }
        
        LogHandler.log_txt(ServerHandler.class.getName(),Level.INFO, "no response from server");
        return false;
    }
}
