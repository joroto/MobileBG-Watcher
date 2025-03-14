import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MobileBGWatcherFrame extends JFrame {
    public static List<Advert> advertList;
    private DefaultListModel<Advert> listModel;
    private JList<Advert> advertJList;
    private JLabel loadingLabel;
    private JPanel loadingPanel;
    private List<String> propertySettings = List.of("logging_enabled");

    public MobileBGWatcherFrame() {
        Utils.loadProperties();
        advertList = new ArrayList<>();
        listModel = new DefaultListModel<>();
        advertJList = new JList<>(listModel);
        advertJList.setCellRenderer(new AdvertInfoCellRenderer());
        advertJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        advertJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = advertJList.locationToIndex(evt.getPoint());
                    openAdvert(listModel.getElementAt(index));
                }
            }

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = advertJList.locationToIndex(e.getPoint());
                    try {
                        Desktop.getDesktop().browse(new URI(listModel.getElementAt(index).getAdvertURL()));
                    } catch (IOException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
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
                Utils.quit();
            }
        });

        Utils.loadFavourites();
        loadCars();
    }

    private void loadCars() {
        loadingLabel.setVisible(true);
        advertJList.setVisible(false);
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
                getContentPane().add(new JScrollPane(advertJList), BorderLayout.CENTER);
                loadingPanel.removeAll();
                revalidate();
                advertJList.setVisible(true);
            }
        };

        worker.execute();
    }

    List<String> modelsScanned = new ArrayList<>();

    private void updateCarInfo() {
        advertList.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(Utils.getProperties().size());
        List<Callable<Void>> tasks = new ArrayList<>();

        Utils.getProperties().stringPropertyNames().forEach(modelName -> {
            if (!propertySettings.contains(modelName)) {
                tasks.add(() -> {
                    String request = Utils.getRequestFromPropertiesFile(modelName);
                    Logger_.info("Getting cars for: " + modelName);
                    Logger_.info("Request: " + request);
                    getCars(request, modelName);
                    Logger_.info(modelName + " loaded.");
                    SwingUtilities.invokeLater(() -> {
                        loadingPanel.add(new JLabel(modelName + " loaded."));
                        loadingPanel.revalidate();
                        loadingPanel.repaint();
                    });
                    return null;
                });
            }
        });

        try {
            executorService.invokeAll(tasks);
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Logger_.error("Error in executing tasks: " + e.getMessage());
        }

//        carList.sort(Comparator.comparingLong(Car::getAdvN).reversed());
        advertList.sort(new AdvertComparator().thenComparing(Comparator.comparingLong(Advert::getAdvertNumber).reversed()));

        listModel.clear();
        for (Advert carInfo : advertList) {
            listModel.addElement(carInfo);
        }
    }

    public class AdvertComparator implements Comparator<Advert> {
        @Override
        public int compare(Advert car1, Advert car2) {
            if (car1.isFavourite() && !car2.isFavourite()) {
                return -1;
            } else if (!car1.isFavourite() && car2.isFavourite()) {
                return 1;
            } else {
                return Long.compare(car2.getAdvertNumber(), car1.getAdvertNumber());
            }
        }
    }

    private void getCars(String reqBody, String modelName) {
        try {
            Document document = Utils.callURL(reqBody);
//                System.out.println(document.toString() + " HTML");
            Elements elements = document.select("form[name='search'] div[class*='item'][id]");
            for (Element element : elements) {
                String carImageURL = "https:" + element.select("img[class='pic']").attr("src");
                if (!carImageURL.contains("/no.gif") && (carImageURL.contains("cdn") || carImageURL.contains("mobistatic"))) {
                    String carLink = element.select("a[class='title saveSlink']").attr("href");
                    Pattern advPattern = Pattern.compile("(adv=|obiava-)(\\d+)");
                    Matcher matcher = advPattern.matcher(carLink);
                    if (matcher.find()) {
                        carLink = "https://" + carLink.replaceFirst("//", "");
                        String advID = matcher.group(2);
                        if (advID.startsWith("2")) {
                            advID = 1 + advID.substring(1);
                        }

                        String advertParams = "";
                        String[] split = element.select("div[class='params']").html().split("<span>");
                        for (int i = 0; i < split.length; i++) {
                            String s = split[i];
                            if (i % 2 == 0 && i != 0) {
                                advertParams += "<br>" + s;
                            } else {
                                advertParams += s;
                            }
                        }

                        Advert advert = new Advert(carImageURL,
                                element.select("a[class*='title']").text(),
                                carLink,
                                element.select(".price ").text().split(" лв.")[0] + " лв.",
                                Long.valueOf(advID),
                                modelName,
                                advertParams
                        );

                        advertList.add(advert);
                    } else {
                        Logger_.error("Failed to get car info, check selectors");
                        Utils.quit();
                    }
                }
            }
//                Logger_.info(carsFound + " cars found!");
            if (!modelsScanned.contains(modelName)) {
                // Go to page 2 and fetch cars also
                modelsScanned.add(modelName);
                getCars(Utils.insertTextBeforeQuestionMark(reqBody, "/p-2", '?'), modelName);
            }
        } catch (Exception e) {
            Logger_.error("getCars() request failed.");
            Logger_.error(e.getMessage());
            Utils.quit();
        }
    }

    Map<String, Advert> fullyloadedAdvertMap = new HashMap<>();

    private void openAdvert(Advert advertIn) {
        try {
            Document document = Utils.callURL(advertIn.getAdvertURL());
//            System.out.println(document + " DOC");
            Elements images = document.select("#owlcarousel img");
            List<String> imageLinks = new ArrayList<>();
            for (Element image : images) {
                String src = image.attr("data-src");
                imageLinks.add(src);
            }

//            Element advertTitle = document.selectFirst("div[class='obTitle']");
            Element carLocation = document.selectFirst("div[class='carLocation']");
//            Element carPrice = document.selectFirst("div[class='Price']");
            Element advertPhone = document.selectFirst("div[class='contactsBox'] div[class='phone']");
//            Element priceHistory = document.selectFirst("div[id='priceHistory']");
            Elements mainCarParams = document.select("div[class='mainCarParams'] div[class*='item']");
            Element advertStats = document.selectFirst("div[class='statistiki'] div[class='text']");
            Element advertDescription = document.selectFirst("div[class='moreInfo'] div[class='text']");
            String advertDescriptionText;
            if (advertDescription != null) {
                advertDescriptionText = document.selectFirst("div[class='moreInfo'] div[class='text']").html();
            } else {
                advertDescriptionText = "--- NO DESCRIPTION ---";
            }

            Map<String, String> mainCarParamsMap = new HashMap<>();
            for (Element mainCarParam : mainCarParams) {
                String mpLabel = mainCarParam.selectFirst("div[class='mpLabel']").text();
                String mpInfo = mainCarParam.selectFirst("div[class='mpInfo']").text();
                mainCarParamsMap.put(mainCarParam.className().replace("item ", ""), mpLabel + ": " + mpInfo);
            }

            Advert advert;
            if (!fullyloadedAdvertMap.containsKey(advertIn.getAdvertURL())) {
                advertIn.setImageUrls(imageLinks);
                advertIn.setAdvertDescription(advertDescriptionText);
                advertIn.setAdvertStats(advertStats.text());
                advertIn.setMainCarParams(mainCarParamsMap);
                advertIn.setAdvertPhone(advertPhone.text());
                advertIn.setCarLocation(carLocation.text());
                advertIn.setPriceHistory(fetchPriceHistory(advertIn.getAdvertNumber().toString(), advertIn.getCarPrice(), "лв."));

//                advert = new Advert(imageLinks, advertIn.getAdvertTitle(), carLocation.text(), advertIn.getCarPrice(), advertPhone.text().split(" ")[0], mainCarParamsMap, advertStats.text(), advertIn.getAdvertURL(), advertDescriptionText);
                advert = advertIn;
                fullyloadedAdvertMap.put(advertIn.getAdvertURL(), advertIn);
            } else {
                advert = fullyloadedAdvertMap.get(advertIn.getAdvertURL());
            }

            SwingUtilities.invokeLater(() -> new AdvertFrame(advert).setVisible(true));

        } catch (Exception e) {
            Logger_.error("Could not open browser");
            Logger_.error(e.getMessage());
        }
    }


    public static String fetchPriceHistory(String advertId, String price, String currency) {
        try {
            String requestUrl = "https://www.mobile.bg/pcgi/subscript.cgi";
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setRequestProperty("Referer", "https://www.mobile.bg/obiava-" + advertId);
            conn.setDoOutput(true);


            String postData = "act=3&ida=" + URLEncoder.encode(advertId, "UTF-8") +
                    "&pr=" + URLEncoder.encode(price, "UTF-8") +
                    "&cr=" + URLEncoder.encode("лв.", "windows-1251") +
                    "&mode=1";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "windows-1251"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class AdvertInfoCellRenderer extends JPanel implements ListCellRenderer<Advert> {
        private final Color GOLD_COLOR = new Color(255, 196, 0);

        private JLabel imageLabel = new JLabel();
        private JLabel textLabel = new JLabel();
        private JLabel priceLabel = new JLabel();
        private JLabel advLabel = new JLabel();
        JPanel textPanel = new JPanel(new BorderLayout());

        public AdvertInfoCellRenderer() {
            setLayout(new BorderLayout());
//            setBorder(new EmptyBorder(0, 0, 10, 0)); // Adds spacing around each cell
//            setBorder(new LineBorder(Color.BLUE, 2));
            setBorder(new MatteBorder(1, 0, 1, 0, Color.BLUE));
            add(imageLabel, BorderLayout.WEST);
            textPanel.add(textLabel, BorderLayout.NORTH);
            textPanel.add(priceLabel, BorderLayout.CENTER);
            textPanel.add(advLabel, BorderLayout.SOUTH);
            add(textPanel, BorderLayout.CENTER);
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList<? extends Advert> list, Advert value, int index, boolean isSelected, boolean cellHasFocus) {
            BufferedImage image = value.getMainImage();
            int targetWidth = 200;
            int targetHeight = (int) ((double) targetWidth / image.getWidth() * image.getHeight());

            Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            textLabel.setText(
                    "<html><p style=\"color:red;\">" + value.getAdvertTitle() + "</p>" +
                            "Price: " + value.getCarPrice() +
//                            "<br> Adv: " + value.getAdvertNumber() +
                            "<br><p style=\"color:blue;\">" + value.getAdvertProperty() + "</p>" +
//                            "<br><br>" + value.getAdvertProperty() +
                            value.getAdvertParams() + "</html>");

            if (value.isFavourite()) {
                setBackground(GOLD_COLOR);
                setBorder(new MatteBorder(1, 0, 1, 0, GOLD_COLOR));
            } else {
                setBackground(list.getBackground());
                setBorder(new MatteBorder(1, 0, 1, 0, Color.GRAY)); // Default border
            }

            textPanel.setOpaque(false);
            imageLabel.setOpaque(false);
            textLabel.setOpaque(false);
            priceLabel.setOpaque(false);
            advLabel.setOpaque(false);

            return this;
        }
    }
}