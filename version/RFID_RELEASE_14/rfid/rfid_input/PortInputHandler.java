package rfid.rfid_input;

import com.fazecast.jSerialComm.*;

import rfid.LogHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class PortInputHandler extends InputHandler {
   private byte[] output = null;
   private SerialPort[] comPortList;
   private SerialPort comPort;
   private ReadWriteLock lock = new ReentrantReadWriteLock();

   private PortInputHandler() {
   }

   public static PortInputHandler getInputHandler() {
      PortInputHandler inputHandler = new PortInputHandler();
      // Exception 1: Cannot connect to device
      inputHandler.comPortList = SerialPort.getCommPorts();
      if (inputHandler.comPortList.length == 0) {
         LogHandler.log_txt(PortInputHandler.class.getName(), Level.INFO, "Fail to connect device");
         System.exit(0);
      }

      inputHandler.comPort = inputHandler.comPortList[0];
      inputHandler.setPortConfig();
      return inputHandler;
   }

   public boolean openPort() {
      // guaranted to have device connected
      comPort.openPort();
      return comPort.isOpen();
   }

   public void closePort() {
      comPort.closePort();
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
         System.exit(0);
      }

      LogHandler.log_txt(PortInputHandler.class.getName(), Level.INFO, "Port is open");
      // wait till there is output
      new_output = getOutput();

      // End communication
      closePort();
      String log_output = String.format("Total bytes = %s", new_output.length);
      LogHandler.log_txt(PortInputHandler.class.getName(), Level.INFO, log_output);

      // output
      String result = "";
      for (byte i : new_output) {
         result += i;
      }

      // nếu đúng 18 bytes, trả về luôn
      if (new_output.length == 18)
         return result;
      // nếu nhỏ hơn 18 bytes, không đủ để xác thực, bỏ
      else if (new_output.length < 18)
         return null;

      // lớn hơn 18 bytes, cắt chuỗi, trả về
      return processBytes(result);
   }

   private void changeData(byte[] output) {
      //System.out.println("Waiting");
      lock.writeLock().lock();
      try {
         //System.out.println("Changing");
         this.output = output;
      } finally {
         //System.out.println("Unlocking");
         lock.writeLock().unlock();
      }
   }

   private byte[] getOutput() {
      byte[] new_output = null;
      while (new_output == null) {
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
         }
         //System.out.println("Waiting");
         lock.readLock().lock();
         try{
            //System.out.println("Getting");
            new_output = this.output;
            this.output = null;
         }finally{
            //System.out.println("Unlocking");
            lock.readLock().unlock();
         }
      }

      return new_output;
   }

   private String processBytes(String bytes){
      //define new Regex Pattern
      Pattern p = Pattern.compile("170-180");
      Matcher m = p.matcher(bytes);
      //find two bounds
      int posi[] = new int[2];
      for(int i=0;i<2;i++){
         if(m.find()) posi[i] = m.start();
      }
      //Cut string
      return bytes.substring(posi[0], posi[1]);
   }
}
