import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Logger_ {
    private static StringBuilder log = new StringBuilder();
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
    static String currentDirectory = System.getProperty("user.dir");
    private static LocalDateTime now;

    public static void warn(String logMessage) {
        appendToLog("[WARN]", logMessage);
        Utils.showMessage(logMessage, "WARNING", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(String logMessage) {
        appendToLog("[INFO]", logMessage);
    }

    public static void error(String logMessage) {
        appendToLog("[ERR]", logMessage);
        Utils.showMessage(logMessage, "ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    private static void appendToLog(String logLevel, String logMessage) {
        now = LocalDateTime.now();
        String message = logLevel + " " + dtf.format(now) + " - " + logMessage + System.lineSeparator();
        log.append(message);
        System.out.print(message);
    }

    public static void saveLog() {
        String folderPath = currentDirectory + "/LOGS";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            boolean created = folder.mkdir(); // mkdirs() can be used to create parent directories as well

            if (created) {
                info("Folder for logs created successfully.");
            } else {
                error("Failed to create folder for logs.");
            }
        } else {
            info("Folder for logs found.");
        }

        File file = new File(currentDirectory + "\\LOGS\\" + "MobileBG-Watcher_LOG_" + System.currentTimeMillis() + "");
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(String.valueOf(log));
            myWriter.close();
            Logger_.info("Log saved to " + file.getPath());
        } catch (IOException ex) {
            Logger_.error("Could not save log.");
            Logger_.error(ex.getMessage());
        }
    }

    public static StringBuilder getLog() {
        return log;
    }
}
