import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;

public class Utils {
    private static final Properties properties = new Properties();
    public static boolean checkUrlInFavouritesFile(String urlToCheck) {
        String filePath = "favourites.txt";

        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.equals(urlToCheck)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                Logger_.error(e.getMessage());
            }
        } else {
            Logger_.info("Favourite file does not exist.");
        }

        return false;
    }

    public static String insertTextBeforeQuestionMark(String originalString, String textToInsert, char beforeChar) {
        int index = originalString.indexOf(beforeChar);
        if (index != -1) {
            String beforeQuestionMark = originalString.substring(0, index);
            String afterQuestionMark = originalString.substring(index);
            return beforeQuestionMark + textToInsert + afterQuestionMark;
        } else {
            return "No " + beforeChar + " found in the original string.";
        }
    }

    public static String getRequestFromPropertiesFile(String modelName) {
        return properties.getProperty(modelName.toLowerCase());
    }

    public static void loadProperties(){
        try {
            FileInputStream input = new FileInputStream("car_requests.properties");
            properties.load(input);
            input.close();
        } catch (IOException e) {
            Logger_.error("NO car_requests.properties FILE FOUND, PLEASE CREATE IT.");
            Logger_.error(e.getMessage());
            quit();
        }
    }

    public static void quit() {
        Logger_.info("Exiting..");
        if (Boolean.parseBoolean(properties.getProperty("logging_enabled"))) {
            Logger_.saveLog();
        }
        System.exit(0);
    }

    public static Properties getProperties(){
        return properties;
    }

    public static Document callURL(String urlIn) {
        Document document = null;

        try {
            document = Jsoup.parse(new URL(urlIn).openStream(), "Windows-1251", urlIn);
        } catch (IOException e) {
            Logger_.error(e.getMessage());
        }

        return document;
    }

    public static void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }
}
