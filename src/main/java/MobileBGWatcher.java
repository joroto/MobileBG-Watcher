import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileBGWatcher extends JFrame {
    private final Properties properties;
    private List<Car> carList;
    private DefaultListModel<Car> listModel;
    private JList<Car> carJList;

    public MobileBGWatcher() {
        this.properties = new Properties();
        try {
            FileInputStream input = new FileInputStream("car_requests.properties");
            properties.load(input);
            input.close();
        } catch (IOException e) {
            Logger_.error("NO car_requests.properties FILE FOUND, PLEASE CREATE IT.");
            Logger_.error(e.getMessage());
        }
        carList = new ArrayList<>();
        listModel = new DefaultListModel<>();
        carJList = new JList<>(listModel);
        carJList.setCellRenderer(new CarInfoCellRenderer());
        carJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        carJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = carJList.locationToIndex(evt.getPoint());
                    openBrowser(listModel.getElementAt(index).getUrl());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(carJList);

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("MobileBG Watcher");
        setSize(400, 800);
        setLocationRelativeTo(null);
        updateCarInfo();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        int refreshInterval;
        if (properties.getProperty("refresh_interval") == null) {
            refreshInterval = 10;
            Logger_.info("Custom refresh interval not set, defaulting to 10 minutes.");
        } else {
            refreshInterval = Integer.parseInt(properties.getProperty("refresh_interval"));
            Logger_.info("Custom refresh interval set at " + refreshInterval + " minutes.");
        }
        new Timer(refreshInterval * 60000, evt -> updateCarInfo()).start();
    }

    private void quit() {
        Logger_.info("Exiting..");
        Logger_.saveLog();
    }

    private String getRequest(String modelName) {
        return properties.getProperty(modelName.toLowerCase());
    }

    private void updateCarInfo() {
        carList.clear();
        properties.stringPropertyNames().forEach(modelName -> {
            String request = getRequest(modelName);
            Logger_.info("Getting cars for: " + modelName);
            Logger_.info("Request: " + request);
            getCars(request);
        });

        carList.sort(Comparator.comparingLong(Car::getAdvN).reversed());

        listModel.clear();
        for (Car carInfo : carList) {
            listModel.addElement(carInfo);
        }
        repaint();
    }

    private void getCars(String reqBody) {
        try {
            URL url = new URL("https://www.mobile.bg/pcgi/mobile.cgi");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            connection.setRequestProperty("accept-language", "en-US,en;q=0.9,bg;q=0.8");
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            byte[] postData = reqBody.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData);
            }

            int responseCode = connection.getResponseCode();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Logger_.info("Response Code: " + responseCode);
                Document document = Jsoup.parse(response.toString());
                Elements elements = document.select("form > .tablereset");
                int carsFound = 0;
                for (Element element : elements) {
                    String carImage = element.select("img").attr("src");
                    if (!carImage.contains("/no.gif") && (carImage.contains("cdn") || carImage.contains("mobistatic"))) {
                        String carLink = element.select("a").attr("href");
                        Pattern advPattern = Pattern.compile("adv=(\\d+)");
                        Matcher matcher = advPattern.matcher(carLink);
                        if (matcher.find()) {
                            BufferedImage read = ImageIO.read(new URL("https:" + carImage));
                            carList.add(new Car(read,
                                    element.select(".mmm").text(),
                                    "https://" + carLink.replaceFirst("//", ""),
                                    element.select(".price").text(),
                                    Long.valueOf(matcher.group(1))
                            ));
                            carsFound++;
                        } else {
                            Logger_.error("Failed to get car info, check selectors");
                            quit();
                        }
                    }
                }
                Logger_.info(carsFound + " cars found!");
            }
            connection.disconnect();
        } catch (Exception e) {
            Logger_.error("getCars() request failed.");
            Logger_.error(e.getMessage());
            quit();
        }
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception e) {
            Logger_.error("Could not open browser");
            Logger_.error(e.getMessage());
        }
    }

    private static class CarInfoCellRenderer extends JPanel implements ListCellRenderer<Car> {
        private JLabel imageLabel = new JLabel();
        private JLabel textLabel = new JLabel();
        private JLabel priceLabel = new JLabel();
        private JLabel advLabel = new JLabel();

        public CarInfoCellRenderer() {
            setLayout(new BorderLayout());
            add(imageLabel, BorderLayout.WEST);
            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.add(textLabel, BorderLayout.NORTH);
            textPanel.add(priceLabel, BorderLayout.CENTER);
            textPanel.add(advLabel, BorderLayout.SOUTH);
            add(textPanel, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(JList<? extends Car> list, Car value, int index, boolean isSelected, boolean cellHasFocus) {
            BufferedImage image = value.getImage();
            int targetWidth = 200;
            int targetHeight = (int) ((double) targetWidth / image.getWidth() * image.getHeight());

            Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            textLabel.setText(
                    "<html>" + value.getTitle() +
                            "<br> Price: " + value.getPrice() +
                            "<br> Adv: " + value.getAdvN() + "</html>");

            return this;
        }
    }
}
