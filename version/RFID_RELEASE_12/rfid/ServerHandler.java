package rfid;

import java.util.logging.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
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

    private static Logger logger = Logger.getLogger(ServerHandler.class.getName());

    private JSONHandler config_handler;
    private BusCSVHandler csv_handler;
    private BusRFIDMapCSVHandler bus_rfid_handler;
    private OutputStreamWriter log_writer;
    private long startOffline = -1;
    private String previousRFID = "";

    public static ServerHandler getServerHandler() {
        ServerHandler server_handler = new ServerHandler();
        server_handler.config_handler = JSONHandler.getJSONHandler().getJSONObjectFromFile(JSONHandler.CONFIGS_DIR);
        server_handler.csv_handler = BusCSVHandler.getBusCSVHandler();
        server_handler.bus_rfid_handler = BusRFIDMapCSVHandler.getBusRFIDMapCSVHandler();

        return server_handler;
    }

    public ServerHandler setWriterForDebug(OutputStreamWriter log_writer) {
        this.log_writer = log_writer;

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

    public void postServer(String RFID_input, String MAC_address) {
        // prepare data into correct format
        Bus bus_info = processInputToBus(RFID_input, MAC_address);
        // update log offline in case there is no network
        csv_handler.updateBusContent(bus_info);
        // write to LOG_OFFLINE for debug purpose
        String log_content = String.format("DATE_TIME = %s, RFID_input = %s, MAC_address = %s %n", bus_info.getDATE_TIME(), RFID_input, MAC_address);
        if (log_writer != null) {
            try {
                log_writer.append(log_content);
                log_writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.log(Level.INFO, log_content);

        //check connection to server
        if(!hasInternetConectivity()){
            logger.log(Level.SEVERE, "No internet connection");
            startOffline = BusCSVHandler.getNewestLine(FileDir.BUS_HISTORY);
            return;
        }

        if(startOffline != -1){
            //new Thread to push to server
            //fuck
            //after this, test, and save a new version
            //then, refactoring code

            //end
            startOffline = -1;
        }

        // push to server
        // prepare data
        HashMap<String, String> map_value = new HashMap<String, String>() {
            private static final long serialVersionUID = -6185188228197884639L;
        };
        map_value.put("DATE_TIME", bus_info.getDATE_TIME());
        map_value.put("RFID_CODE", bus_info.getRFID_CODE());
        map_value.put("MAC_ADDRESS", bus_info.getMAC_ADDRESS());

        // push data to server
        String requestBody = JSONHandler.getJSONHandler().convertHashMapToJSONString(map_value);
        logger.log(Level.INFO, requestBody);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.config_handler.getField(JSONField.URL)))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            // MEGUMIN: further change to here
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        if (response != null)
            logger.log(Level.INFO, response.body());
        else
            logger.log(Level.INFO, "no response from server");
    }

    private Bus processInputToBus(String RFID_input, String MAC_address) {
        String DATE_TIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

        return new Bus(RFID_input, DATE_TIME, MAC_address);
    }

    private boolean hasInternetConectivity() {
        try {
            URL url = new URL(config_handler.getField(JSONField.URL));
            URLConnection connection = url.openConnection();
            connection.connect();
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        
        return true;
    }
}
