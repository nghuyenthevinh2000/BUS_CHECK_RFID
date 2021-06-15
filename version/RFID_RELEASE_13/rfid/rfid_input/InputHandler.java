package rfid.rfid_input;

import java.util.logging.Logger;

import rfid.LogHandler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.net.NetworkInterface;
import java.net.SocketException;

public abstract class InputHandler{
    private static Logger logger = Logger.getLogger(InputHandler.class.getName());

    public abstract String getRFIDInput();

    public String getLocalMacAddress(int physical_position){
        ArrayList<String> mac_address = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                byte [] mac = e.nextElement().getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++)
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    mac_address.add(sb.toString());
                    logger.log(Level.INFO,sb.toString());
                }
            }
        } catch (SocketException e) {
            LogHandler.log_err(InputHandler.class.getName(), e);
        }

        return mac_address.get(physical_position);
    }
}
