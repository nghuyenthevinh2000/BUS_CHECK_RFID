package rfid.rfid_input;

import com.fazecast.jSerialComm.*;

import rfid.JSONHandler;
import rfid.LogHandler;
import rfid.MainProgram;
import rfid.FileHandler.FileDir;
import rfid.JSONHandler.JSONField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;

public class PortInputHandler extends InputHandler {
   private byte[] output = null;
   private SerialPort[] comPortList;
   private SerialPort comPort;
   private JSONHandler json;

   private PortInputHandler() {
   }

   public static PortInputHandler getInputHandler() {
      PortInputHandler inputHandler = new PortInputHandler();
      // Exception 1: Cannot connect to device
      inputHandler.comPortList = SerialPort.getCommPorts();
      if (inputHandler.comPortList.length == 0) {
         LogHandler.log_txt(PortInputHandler.class.getName(), Level.INFO, "Fail to connect device");
         MainProgram.exitProgram();
      }
      inputHandler.json = JSONHandler.getJSONHandler(FileDir.CONFIGS_DIR).getJSONObjectFromFileConfig();

      inputHandler.comPort = inputHandler.comPortList[0];
      inputHandler.setPortConfig();
      return inputHandler;
   }

   public boolean openPort() {
      // guaranted to have device connected
      comPort.openPort();
      return comPort.isOpen();
   }

   public PortInputHandler closePort() {
      comPort.closePort();
      return this;
   }

   public void setPortConfig() {
      comPort.setBaudRate(38400);
      comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
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
            comPort.readBytes(newData, availableByte);
            changeData(newData);
         }
      });
   }

   public String getRFIDInput() {
      // reset output state
      byte[] new_output = null;
      // Ready for communication
      openPort();

      // check if port is open
      if (!comPort.isOpen()) {
         LogHandler.log_txt(PortInputHandler.class.getName(), Level.SEVERE, "Port is occupied, now exit");
         MainProgram.exitProgram();
      }

      LogHandler.log_txt(PortInputHandler.class.getName(), Level.INFO, "Port is open");
      // wait till there is output or 3 minutes have passed. 
      //if wait for too long, program should be checking program state.
      new_output = getOutput();
      if(new_output == null)
         return null;

      // End communication
      closePort();

      // output
      StringBuilder result = new StringBuilder();
      for (byte i : new_output) {
         result.append(i);
      }
      String log_output = String.format("Total bytes = %s, RFID_CODE = %s", new_output.length, result.toString());
      LogHandler.log_txt(PortInputHandler.class.getName(), Level.INFO, log_output);

      // nếu đúng 18 bytes, trả về luôn
      if (new_output.length == 18)
         return result.toString();
      // nếu nhỏ hơn 18 bytes, không đủ để xác thực, bỏ
      else if (new_output.length < 18)
         return null;

      // lớn hơn 18 bytes, cắt chuỗi, trả về
      return processBytes(result.toString());
   }

   private synchronized void changeData(byte[] output) {
      this.output = output;
      notifyAll();
   }

   private synchronized byte[] getOutput() {
      byte[] new_output = null;
      while (this.output == null) {
         try {
            wait(Long.parseLong(json.getField(JSONField.READ_DATA_TIME_OUT_MS)));
            if(this.output == null) 
               return new_output;
         } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
         }
      }

      new_output = this.output;
      this.output = null;

      return new_output;
   }

   private String processBytes(String bytes){
      //define new Regex Pattern
      Pattern p = Pattern.compile("170-180");
      Matcher m = p.matcher(bytes);
      //find two bounds
      int[] posi = new int[2];
      for(int i=0;i<2;i++){
         if(m.find()) posi[i] = m.start();
      }
      //Cut string
      //cho trường hợp các mã sai series, đúng cấu trúc (> 18 bytes)
      if(posi[0] > posi[1])
         return null;

      return bytes.substring(posi[0], posi[1]);
   }
}
