package rfid_gui;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import rfid_gui.FileHandler.FileDir;

public class LogHandler {
    // this class is used for debug purpose only
    private LogHandler() {}

    public static void log_txt(String class_name, Level level, String str) {
        OutputStreamWriter writer = FileHandler.getWriter(FileDir.LOG_OFFLINE, true);
        String DATE_TIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        String body = String.format("%s: %s: %s: %s %n", DATE_TIME, class_name, level.getName(), str);
        try {
            writer.append(body);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LogHandler.log_err(LogHandler.class.getName(), e);
        }
    }

    public static void reset_txt(){
        OutputStreamWriter writer = FileHandler.getWriter(FileDir.LOG_OFFLINE, false);
        try {
            writer.write("");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public static void log_terminal(String class_name, Level level, String str){
        Logger.getLogger(class_name).log(level, str);
    }

    public static void log_err(String class_name, Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        LogHandler.log_txt(class_name, Level.SEVERE, stackTrace); 
    }

}
