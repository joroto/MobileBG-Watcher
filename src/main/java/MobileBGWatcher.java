import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.Timer;
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
import java.util.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileBGWatcher extends JFrame {
    private final Properties properties;
    private List<Car> carList;
    private DefaultListModel<Car> listModel;
    private JList<Car> carJList;
    private JLabel loadingLabel;
    private JPanel loadingPanel;

    public MobileBGWatcher() {
        this.properties = new Properties();
        try {
            FileInputStream input = new FileInputStream("car_requests.properties");
            properties.load(input);
            input.close();
        } catch (IOException e) {
            Logger_.error("NO car_requests.properties FILE FOUND, PLEASE CREATE IT.");
            Logger_.error(e.getMessage());
            quit();
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

        loadingLabel = new JLabel("Loading cars...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Serif", Font.BOLD, 20));

        loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.add(loadingLabel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(loadingPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("MobileBG Watcher");
        setSize(400, 800);
        setLocationRelativeTo(null);

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
        new Timer(refreshInterval * 60000, evt -> loadCars()).start();

        loadCars();
    }

    private void quit() {
        Logger_.info("Exiting..");
        Logger_.saveLog();
        System.exit(0);
    }

    private String getRequest(String modelName) {
        return properties.getProperty(modelName.toLowerCase());
    }

    static int carsFound = 0;

    private void loadCars() {
        loadingLabel.setVisible(true);
        carJList.setVisible(false);
        loadingPanel.removeAll();
        loadingPanel.add(loadingLabel);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                updateCarInfo();
                return null;
            }

            @Override
            protected void done() {
                loadingLabel.setVisible(false);
                getContentPane().removeAll();
                getContentPane().add(new JScrollPane(carJList), BorderLayout.CENTER);
                loadingPanel.removeAll();
                revalidate();
                carJList.setVisible(true);
            }
        };

        worker.execute();
    }

    List<String> modelsScanned = new ArrayList<>();
    private void updateCarInfo() {
        carList.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(properties.size()); // Adjust the pool size as needed
        List<Callable<Void>> tasks = new ArrayList<>();

        properties.stringPropertyNames().forEach(modelName -> {
            tasks.add(() -> {
                String request = getRequest(modelName);
                Logger_.info("Getting cars for: " + modelName);
                Logger_.info("Request: " + request);
                carsFound = 0;
                getCars(request, modelName);
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.add(new JLabel(modelName + " loaded."));
                    loadingPanel.revalidate();
                    loadingPanel.repaint();
                });
                return null;
            });
        });

        try {
            executorService.invokeAll(tasks);
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Logger_.error("Error in executing tasks: " + e.getMessage());
        }

        carList.sort(Comparator.comparingLong(Car::getAdvN).reversed());

        listModel.clear();
        for (Car carInfo : carList) {
            listModel.addElement(carInfo);
        }
    }

    private void getCars(String reqBody, String modelName) {
        try {
//            System.out.println(reqBody);
            URL url = new URL(reqBody);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            connection.setRequestProperty("accept-language", "en-US,en;q=0.9,bg;q=0.8");
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

//            byte[] postData = reqBody.getBytes(StandardCharsets.UTF_8);
//            try (OutputStream os = connection.getOutputStream()) {
//                os.write(postData);
//            }

            int responseCode = connection.getResponseCode();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Logger_.info("Response Code: " + responseCode);
                Document document = Jsoup.parse(response.toString());
//                System.out.println(document.toString() + " HTML");
                Elements elements = document.select("form[name='search'] div[class*='item']");
                for (Element element : elements) {
                    String carImageURL = "https:" + element.select("img[class='pic']").attr("src");
                    if (!carImageURL.contains("/no.gif") && (carImageURL.contains("cdn") || carImageURL.contains("mobistatic"))) {
                        String carLink = element.select("a[class='title saveSlink']").attr("href");
                        Pattern advPattern = Pattern.compile("(adv=|obiava-)(\\d+)");
                        Matcher matcher = advPattern.matcher(carLink);
                        if (matcher.find()) {
                            String advID = matcher.group(2);
                            if(advID.startsWith("2")){
                                advID = 1 + advID.substring(1);
                            }

                            carList.add(new Car(carImageURL,
                                    element.select(".zaglavie").text(),
                                    "https://" + carLink.replaceFirst("//", ""),
                                    element.select(".price ").text(),
                                    Long.valueOf(advID)
                            ));
                            carsFound++;
                        } else {
                            Logger_.error("Failed to get car info, check selectors");
                            quit();
                        }
                    }
                }
//                Logger_.info(carsFound + " cars found!");
                if (!modelsScanned.contains(modelName)) {
                    // Go to page 2 and fetch cars also
                    modelsScanned.add(modelName);
                    getCars(insertTextBeforeQuestionMark(reqBody, "/p-2", '?'), modelName);
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            Logger_.error("getCars() request failed.");
            Logger_.error(e.getMessage());
            quit();
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
