package rfid;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class InputHandler{
    private static Logger logger = Logger.getLogger(InputHandler.class.getName());

    public abstract String getRFIDInput();

    public String getLocalMacAddress(){
        String MAC_address = null;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            MAC_address = InputHandler.getMacAddress(ip);
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }

        return MAC_address;
    }

    private static String getMacAddress(InetAddress ip) {
        String address = null;
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            //nếu như không kết nối được với wifi, không có ip, lỗi Null Pointer Exception.
            //làm sao để liên tục thử bắt lại với mạng.
            //nếu như không bắt được mạng, ghi tạm vào máy offline.
            //đợi cho đến khi nào kết nối lại được với mạng thì đẩy lên.
            if(network == null){
                
            }
            byte[] mac = network.getHardwareAddress();


            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            address = sb.toString();

        } catch (SocketException ex) {

            ex.printStackTrace();

        }

        return address;
    }
}
