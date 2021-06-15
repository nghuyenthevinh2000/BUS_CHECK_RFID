package rfid;

import com.fazecast.jSerialComm.*;

import rfid.JSONHandler.JSONField;

import java.util.logging.Logger;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.logging.Level;

public class PortInputHandler extends InputHandler {
   private static Logger logger = Logger.getLogger(PortInputHandler.class.getName());
   private byte[] output = null;
   private SerialPort[] comPortList;
   private SerialPort comPort;
   private JSONHandler config_handler;
   private OutputStreamWriter writer;

   private PortInputHandler() {
   }

   public static PortInputHandler getInputHandler() {
      PortInputHandler inputHandler = new PortInputHandler();
      inputHandler.config_handler = JSONHandler.getJSONHandler().getJSONObjectFromFile(JSONHandler.CONFIGS_DIR);
      int retry_time = Integer.parseInt(inputHandler.config_handler.getField(JSONField.DEVICE_CONNECT_RETRY_TIME));
      // Exception 1: Cannot connect to device
      while (true) {
         inputHandler.comPortList = SerialPort.getCommPorts();
         if (inputHandler.comPortList.length > 0)
            break;

         inputHandler.deviceErrorHandler();
         logger.log(Level.INFO, "Fail to connect device");
         try {
            Thread.sleep(Long.parseLong(inputHandler.config_handler.getField(JSONField.DEVICE_CONNECT_FAIL_TIME_OUT_MS)));
         } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
         }

         //if has retried retry_time, exit program
         retry_time--;
         if (retry_time == 0)
            System.exit(0);
      }

      inputHandler.comPort = inputHandler.comPortList[0];
      return inputHandler;
   }

   //set writer for appending log data to log.txt for debug purpose
   public PortInputHandler setWriterForDebug(OutputStreamWriter writer){
      this.writer = writer;

      return this;
   }

   public boolean openPort() {
      // guaranted to have device connected
      comPort.openPort();
      return comPort.isOpen();
   }

   public void setPortConfig() {
      comPort.setBaudRate(38400);
      comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
   }

   public void closePort() {
      comPort.closePort();
   }

   public String getRFIDInput() {
      setPortConfig();
      openPort();

      //check if port is open
      while (!comPort.isOpen()) {
         logger.log(Level.SEVERE, "Wait for port to open");
         try {
            Thread.sleep(3000);
         } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
         }
      }

      //listen for data and handle data
      logger.log(Level.INFO, "Port is open");
      comPort.addDataListener(new SerialPortDataListener() {
         @Override
         public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
         }

         @Override
         public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
               return;
            int availableByte = comPort.bytesAvailable();
            byte[] newData = new byte[availableByte];
            int readByte = comPort.readBytes(newData, availableByte);
            output = newData;

            //write to LOG_OFFLINE for debug purpose
            //if(availableByte != 8) return;
            String log_content = String.format("Total bytes = %s, Bytes failed to read = %s", availableByte, availableByte - readByte);
            if (writer != null) {
               try {
                  writer.append(log_content);
                  writer.append("\n");
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }

            logger.log(Level.INFO, log_content);
         }
      });
      closePort();
      if(output == null) return null;

      //output
      //String result = Base64.getEncoder().encodeToString(output);
      String result = "";
      for(byte i : output){
         result += i;
      }
      output = null;
      return result;
   }

   private boolean deviceErrorHandler(){
      //MEGUMIN: build this
      return ServerHandler.postServerNoti("shit");
   }
}
