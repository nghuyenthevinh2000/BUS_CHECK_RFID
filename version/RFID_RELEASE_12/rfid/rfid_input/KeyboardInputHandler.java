package rfid.rfid_input;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class KeyboardInputHandler extends InputHandler {
    private Scanner io = new Scanner(System.in);
    private Logger logger = Logger.getLogger(KeyboardInputHandler.class.toString());

    private KeyboardInputHandler(){}

    public static KeyboardInputHandler getInputHandler(){
        return new KeyboardInputHandler();
    }

    public String getRFIDInput(){
        return io.nextLine();
    }
}