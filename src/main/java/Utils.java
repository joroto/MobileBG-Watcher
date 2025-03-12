import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Utils {
    private static final Properties properties = new Properties();
    private static final List<String> favourites = new ArrayList<>();

    public static boolean checkUrlInFavourites(String url) {
        if (favourites.contains(url)) {
            return true;
        } else {
            return false;
        }
    }

    public static void loadFavourites() {
        Logger_.info("Loading favourites list..");
        String filePath = "favourites.txt";

        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    favourites.add(line);
                }
                Logger_.info("Favourites list loaded:");
                Logger_.info(String.valueOf(favourites));
            } catch (IOException e) {
                Logger_.error(e.getMessage());
            }
        } else {
            Logger_.info("Favourite file does not exist.");
        }
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

    public static void loadProperties() {
        File file = new File("car_requests.properties");

        if (!file.exists()) {
            Logger_.warn("car_requests.properties file does not exist! Creating default one... Please edit it with your desired filters");
            createDefaultProperties(file);
        }

        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException e) {
            Logger_.error("Error loading car_requests.properties file.");
            Logger_.error(e.getMessage());
            quit();
        }
    }

    private static void createDefaultProperties(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("vw_golf=https://www.mobile.bg/obiavi/avtomobili-dzhipove/vw/golf/benzinov/rachna/oblast-sofiya?sort=6\n");
            writer.write("toyota_yaris=https://www.mobile.bg/obiavi/avtomobili-dzhipove/toyota/yaris/benzinov/rachna/oblast-sofiya?sort=6\n");
            Logger_.info("Created default car_requests.properties file.");
        } catch (IOException e) {
            Logger_.error("Failed to create car_requests.properties file.");
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

    public static Properties getProperties() {
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
