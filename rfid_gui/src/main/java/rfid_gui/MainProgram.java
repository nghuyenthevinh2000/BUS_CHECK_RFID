package rfid_gui;

import java.awt.GridBagLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import rfid_gui.rfid_input.PortInputHandler;

public class MainProgram {
    private static PortInputHandler input = null;
    private static JFrame frame;
    private static JTextArea textArea;
    private static boolean stop = false;

    public static void main(String[] arg) {
        try{
            displayGUI();
        }catch(Throwable e){
            LogHandler.log_err(MainProgram.class.getName(), e);
        }
    }

    private static void displayGUI(){
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("RFID READER");

        //set up the panel
        handlePane(frame.getContentPane());
        
        frame.pack();
        frame.setVisible(true);
    }

    private static void handlePane(Container panel){
        JButton button;

        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // add open Port button
        button = new JButton("Open port");
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        button.addActionListener((ActionEvent e) -> {
            if(input != null){
                logPopUp("Port is occupied");
                return;
            }

            input = PortInputHandler.getInputHandler();
        });
        panel.add(button, c);

        // add close Port button
        button = new JButton("Close port");
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        button.addActionListener((ActionEvent e) -> {
            if(input != null){
                input.closePort();
            }

            input = null;
        });
        panel.add(button, c);

        // add Log screen
        textArea = new JTextArea(9, 5);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        c.gridheight = 9;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textArea, c);
        c.gridwidth = 1;
        c.gridheight = 1;

        // add "Clear log" button
        button = new JButton("Clear log");
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        button.addActionListener((ActionEvent e) -> {
            clearTextArea();
        });
        panel.add(button, c);

        // add start getting data
        button = new JButton("Get data");
        c.gridx = 3;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        button.addActionListener((ActionEvent e) -> {
            if(input == null){
                logPopUp("No device is connected");
                return;
            }
            int i = 10;
            setContinue();
            while (i > 1) {
                if(checkStop()) 
                    break;
                String RFID_input = input.getRFIDInput();
                if (RFID_input == null) continue;
                updateTextArea(String.format("RFID_input = %s", RFID_input));
            }
        });
        panel.add(button, c);

        // add stop getting data
        button = new JButton("Stop data");
        c.gridx = 4;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        button.addActionListener((ActionEvent e) -> {
            setStop();
        });
        panel.add(button, c);
    }

    // dieu do doan gang cho textArea
    private static synchronized void updateTextArea(String text){
        textArea.append(text + "\n");
    }

    // clear textArea
    private static synchronized void clearTextArea(){
        textArea.selectAll();
        textArea.replaceSelection("");
    }

    // set stop
    private static synchronized void setStop(){
        stop = true;
    }

    // set continue
    private static synchronized void setContinue(){
        stop = false;
    }

    // check stop state
    private static synchronized boolean checkStop(){
        return stop;
    }

    public static void logPopUp(String text){
        JOptionPane.showMessageDialog(
            frame, 
            text,
            "Warning",
            JOptionPane.WARNING_MESSAGE
        );
    }
}