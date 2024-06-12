import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class AdvertFrame extends JFrame {
    List<String> imageUrls;
    private JLabel imageLabel;
    private int currentIndex = 0;
    JPanel infoPanel;

    public AdvertFrame(Advert advert) {
        imageUrls = advert.getImageUrls();
        setTitle(advert.getAdvertTitle());
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel titleLabel = new JLabel(advert.getAdvertTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel imagePanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateImage();

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openZoomedFrame();
            }
        });

        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JButton previousPictureButton = new JButton("<");
        previousPictureButton.addActionListener(e -> {
            currentIndex = (currentIndex - 1 + imageUrls.size()) % imageUrls.size();
            updateImage();
        });
        imagePanel.add(previousPictureButton, BorderLayout.WEST);

        JButton nextPictureButton = new JButton(">");
        nextPictureButton.addActionListener(e -> {
            currentIndex = (currentIndex + 1) % imageUrls.size();
            updateImage();
        });
        imagePanel.add(nextPictureButton, BorderLayout.EAST);

        add(imagePanel, BorderLayout.WEST);

        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        Font labelFont = new Font("Arial", Font.BOLD, 13);

        JLabel jLabelCarLocation = new JLabel(advert.getCarLocation());
        jLabelCarLocation.setFont(labelFont);
        infoPanel.add(jLabelCarLocation);
        addSpace();

        JLabel jLabelCarPrice = new JLabel(advert.getCarPrice());
        jLabelCarPrice.setFont(labelFont);
        infoPanel.add(jLabelCarPrice);
        addSpace();

        Map<String, String> mainCarParams = advert.getMainCarParams();

        for (String s : mainCarParams.keySet()) {
            JLabel jLabelCarParams = new JLabel(advert.getMainCarParams().get(s));
            jLabelCarParams.setFont(labelFont);
            infoPanel.add(jLabelCarParams);
            addSpace();
        }

        JLabel jLabelAdvertPhone = new JLabel(advert.getAdvertPhone());
        jLabelAdvertPhone.setFont(labelFont);
        infoPanel.add(jLabelAdvertPhone);
        addSpace();

        JLabel jLabelAdvertStats = new JLabel("<html>" + advert.getAdvertStats());
        jLabelAdvertStats.setFont(labelFont);
        infoPanel.add(jLabelAdvertStats);
        addSpace();

        JLabel jLabelAdvertDescription = new JLabel("<html>" + advert.getAdvertDescription());
        jLabelAdvertDescription.setFont(labelFont);
        infoPanel.add(jLabelAdvertDescription);
        addSpace();

        add(infoPanel, BorderLayout.CENTER);

        JButton openAdvertButton = new JButton("GO TO ADVERT");
        openAdvertButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URL(advert.getAdvertURL()).toURI());
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
        add(openAdvertButton, BorderLayout.SOUTH);

        JButton favouriteAdvertButton = new JButton("FAVOURITE");
        Color background = favouriteAdvertButton.getBackground();

        if (advert.isFavourite) {
            favouriteAdvertButton.setBackground(new Color(198, 174, 61));
        } else {
            favouriteAdvertButton.setBackground(background);
        }

        favouriteAdvertButton.addActionListener(e -> {
            if (!advert.isFavourite) {
                advert.setFavourite(true);
                String filePath = "favourites.txt";

                Path path = Paths.get(filePath);
                boolean fileExists = Files.exists(path);

                try {
                    if (fileExists) {
                        Files.write(path, (advert.getAdvertURL() + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                        Logger_.info(advert.getAdvertURL() + " appended to favourites file.");
                    } else {
                        Files.write(path, (advert.getAdvertURL() + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE);
                        Logger_.info("Favourites file created.");
                        Logger_.info(advert.getAdvertURL() + " appended to favourites file.");
                    }
                } catch (IOException ex) {
                    Logger_.error(ex.getMessage());
                }

                favouriteAdvertButton.setBackground(new Color(198, 174, 61));
                Logger_.info("Advert " + advert.getAdvertURL() + " added to favourites");
            } else {
                advert.setFavourite(false);

                try {
                    removeLineFromFile("favourites.txt", advert.getAdvertURL());
                    Logger_.info(advert.getAdvertURL() + " removed from favourites file.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                advert.setFavourite(false);
                favouriteAdvertButton.setBackground(background);
                Logger_.info("Advert " + advert.getAdvertTitle() + " removed from favourites");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(openAdvertButton);
        buttonPanel.add(favouriteAdvertButton);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose(); // Close the frame
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void openZoomedFrame() {
        try {
            URL url = new URL(imageUrls.get(currentIndex));

            new ZoomedFrame(new URL(url.toString().replace("big/", "big1/")));
        } catch (Exception ex) {
            Logger_.error(ex.getMessage());
        }
    }

    public static void removeLineFromFile(String filePath, String urlToRemove) throws IOException {
        File inputFile = new File(filePath);
        File tempFile = new File("temp.txt");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine;

        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.equals(urlToRemove)) {
                continue;
            }
            writer.write(currentLine + System.lineSeparator());
        }

        writer.close();
        reader.close();

        if (!inputFile.delete()) {
            System.out.println("Could not delete the original file.");
            return;
        }

        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename the temporary file.");
        }
    }

    private void addSpace() {
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private int maxImageHeight = 0;
    private void updateImage() {
        try {
            URL url = new URL(imageUrls.get(currentIndex));
            ImageIcon imageIcon = new ImageIcon(url);
            imageLabel.setIcon(imageIcon);

            int currentImageHeight = imageIcon.getIconHeight();
            if (currentImageHeight > maxImageHeight) {
                maxImageHeight = currentImageHeight;
                setSize(1200, maxImageHeight + 100);
            }

            revalidate();
        } catch (Exception e) {
            Logger_.error(e.getMessage());
            imageLabel.setText("Failed to load image");
        }
    }
}
