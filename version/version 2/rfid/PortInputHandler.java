package rfid;

import com.fazecast.jSerialComm.*;

import rfid.JSONHandler.JSONField;

import java.util.logging.Logger;
import java.util.Base64;
import java.util.logging.Level;

public class PortInputHandler extends InputHandler{
   private static Logger logger = Logger.getLogger(PortInputHandler.class.getName());
   private static String CONFIGS_FILE = "config/configs.json";
   private byte[] output = null;
   private SerialPort[] comPortList;
   private SerialPort comPort;
   private JSONHandler config_handler;

   private PortInputHandler(){}

   public static PortInputHandler getInputHandler(){
      PortInputHandler inputHandler = new PortInputHandler();
      inputHandler.config_handler = JSONHandler.getJSONHandler().getJSONObjectFromFile(CONFIGS_FILE);
      int retry_time = Integer.parseInt(inputHandler.config_handler.getField(JSONField.DEVICE_CONNECT_RETRY_TIME));
      //Exception 1: Cannot connect to device
      while(true){
         inputHandler.comPortList = SerialPort.getCommPorts();
         if(inputHandler.comPortList.length > 0) break;

         inputHandler.deviceErrorHandler();
         logger.log(Level.INFO, "Fail to connect device");
         try{
            Thread.sleep(Long.parseLong(inputHandler.config_handler.getField(JSONField.DEVICE_CONNECT_FAIL_TIME_OUT_MS)));
         }catch(InterruptedException e){
            e.printStackTrace();
            Thread.currentThread().interrupt();
         }
         retry_time--;
         if(retry_time == 0) System.exit(0);
      }

      inputHandler.comPort = inputHandler.comPortList[0];
      return inputHandler;
   }

   public boolean openPort(){
      //guaranted to have device connected
      comPort.openPort();
      return comPort.isOpen();
   }

   public void setPortConfig(){
      comPort.setBaudRate(9600);
      comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
   }

   public void closePort(){
      comPort.closePort();
   }

   public String getRFIDInput() {
      setPortConfig();
      openPort();
      
      while(!comPort.isOpen()){
         logger.log(Level.SEVERE, "Wait for port to open");
         try{
            Thread.sleep(3000);
         }catch(InterruptedException e){
            e.printStackTrace();
            Thread.currentThread().interrupt();
         }
      }

      logger.log(Level.INFO, "Port is open");
      comPort.addDataListener(new SerialPortDataListener() {
         @Override
         public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
         }

         @Override
         public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
            int availableByte = comPort.bytesAvailable();
            if(availableByte < 8) return;
            byte[] newData = new byte[availableByte];
            int readByte = comPort.readBytes(newData, availableByte);
            output = newData;
            logger.log(Level.INFO, String.format("Total bytes = %s | Bytes failed to read = %s", availableByte, availableByte - readByte));
         }
      });
      closePort();
      if(output == null) return null;

      //output
      String result = Base64.getEncoder().encodeToString(output);
      output = null;
      return result;
   }

   private boolean deviceErrorHandler(){
      //MEGUMIN: build this
      return ServerHandler.postServerNoti("shit");
   }
}
