package rfid.rfid_input;

import java.util.Scanner;

public class KeyboardInputHandler extends InputHandler {
    private Scanner io = new Scanner(System.in);

    private KeyboardInputHandler(){}

    public static KeyboardInputHandler getInputHandler(){
        return new KeyboardInputHandler();
    }

    public String getRFIDInput(){
        return io.nextLine();
    }
}