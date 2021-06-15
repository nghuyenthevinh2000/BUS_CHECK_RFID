package rfid;

import java.util.logging.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import rfid.JSONHandler.JSONField;
import rfid.csvhandler.Bus;
import rfid.csvhandler.BusCSVHandler;
import rfid.csvhandler.BusRFIDMapCSVHandler;

public class ServerHandler {
    private ServerHandler(){}
    private static final String CONFIGS_DIR = "config/configs.json";
    private static Logger logger = Logger.getLogger(ServerHandler.class.getName());
    
    private JSONHandler config_handler;
    private BusCSVHandler csv_handler;
    private BusRFIDMapCSVHandler bus_rfid_handler;

    public static ServerHandler getServerHandler(){
        ServerHandler server_handler = new ServerHandler();
        server_handler.config_handler = JSONHandler.getJSONHandler().getJSONObjectFromFile(CONFIGS_DIR);
        server_handler.csv_handler = BusCSVHandler.getBusCSVHandler();
        server_handler.bus_rfid_handler = BusRFIDMapCSVHandler.getBusRFIDMapCSVHandler();

        return server_handler;
    }
    
    public void postServer(String RFID_input, String MAC_address){
        Bus bus_info = processInputToBus(RFID_input, MAC_address);
        //update log offline
        csv_handler.updateBusContent(bus_info);
        
        //push to server
        HashMap<String, String> map_value = new HashMap<String, String>(){
            private static final long serialVersionUID = -6185188228197884639L;
        };
        map_value.put("RFID_CODE", bus_info.getRFID_CODE());
        map_value.put("MAC_ADDRESS", bus_info.getMAC_ADDRESS());
        if(bus_info.getBUS_NUMBER_PLATE().isEmpty()) map_value.put("BUS_NUMBER_PLATE", "NULL");
        else map_value.put("BUS_NUMBER_PLATE", bus_info.getBUS_NUMBER_PLATE());

        String requestBody = JSONHandler.getJSONHandler().convertHashMapToJSONString(map_value);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.config_handler.getField(JSONField.URL))).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        HttpResponse<String> response = null;
        try{
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }catch(InterruptedException | IOException e){
            //MEGUMIN: further change to here
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        if(response != null) logger.log(Level.INFO, response.body());
        else logger.log(Level.INFO, "no response");
    }

    public static boolean postServerNoti(String json_body){
        //MEGUMIN: nghĩ ra cách tốt hơn để giải quyết đống mess ở postServer. Thiết kế lại phần này
        return false;
    }

    private Bus processInputToBus(String RFID_input, String MAC_address){
        String BUS_STATION = config_handler.getField(JSONHandler.JSONField.BUS_STATION);
        String BUS_NUMBER_PLATE = bus_rfid_handler.getBUS_NUMBER_PLATEforRFID_CODE(RFID_input);
        Date DATE_TIME = new Date();
        Bus prev_bus = csv_handler.readBusByRFID_CODE(RFID_input);
        int NUMBER_OF_IN = prev_bus.getNUMBER_OF_IN();
        int NUMBER_OF_OUT = prev_bus.getNUMBER_OF_OUT();
        boolean IS_IN = false;

        if(MAC_address.equals(config_handler.getField(JSONHandler.JSONField.MAC_ADDRESS_AS_IN))){
            NUMBER_OF_IN++;
            IS_IN = true;
        }
        else if(MAC_address.equals(config_handler.getField(JSONHandler.JSONField.MAC_ADDRESS_AS_OUT))){
            NUMBER_OF_OUT++;
        }
        else{
            logger.log(Level.SEVERE, "HOLY FUCK! MAC ADDRESS IS WRONG!");
        }

        return new Bus(BUS_STATION, BUS_NUMBER_PLATE, RFID_input, DATE_TIME, NUMBER_OF_IN, NUMBER_OF_OUT, IS_IN, MAC_address);
    }

}
