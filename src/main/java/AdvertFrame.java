import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvertFrame extends JFrame {
    List<String> imageUrls;
    Map<Integer, BufferedImage> loadedImages;
    private JLabel imageLabel;
    private int currentIndex = 0;
    JPanel infoPanel;

    public AdvertFrame(Advert advert) {
        imageUrls = advert.getImageUrls();
        loadedImages = new HashMap<>();
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
            prefetchPreviousImage();
        });
        imagePanel.add(previousPictureButton, BorderLayout.WEST);

        JButton nextPictureButton = new JButton(">");
        nextPictureButton.addActionListener(e -> {
            currentIndex = (currentIndex + 1) % imageUrls.size();
            updateImage();
            prefetchNextImage();
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
        prefetchNextImage();
        prefetchPreviousImage();
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
            Logger_.error("Could not delete the original file.");
            return;
        }

        if (!tempFile.renameTo(inputFile)) {
            Logger_.error("Could not rename the temporary file.");
        }
    }

    private void addSpace() {
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void prefetchNextImage() {
        int prefetchIndex = (currentIndex + 1) % imageUrls.size();
        if (!loadedImages.containsKey(prefetchIndex)) {
            Logger_.info("Prefetching next image: " + prefetchIndex + " " + imageUrls.get(prefetchIndex));
            SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
                @Override
                protected BufferedImage doInBackground() {
                    try {
                        URL url = new URL(imageUrls.get(prefetchIndex));
                        return ImageIO.read(url); // Downloads image in the background
                    } catch (Exception e) {
                        Logger_.error("Failed to prefetch image: " + e.getMessage());
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        BufferedImage img = get();
                        if (img != null) {
                            loadedImages.put(prefetchIndex, img);
                            Logger_.info("Prefetched next image: " + prefetchIndex + " stored.");
                        }
                    } catch (Exception e) {
                        Logger_.error("Error storing prefetched image: " + e.getMessage());
                    }
                }
            };

            worker.execute();
        }
    }

    private void prefetchPreviousImage() {
        int prefetchIndex = (currentIndex - 1 + imageUrls.size()) % imageUrls.size();

        if (!loadedImages.containsKey(prefetchIndex)) {
            Logger_.info("Prefetching previous image: " + prefetchIndex + " " + imageUrls.get(prefetchIndex));

            SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
                @Override
                protected BufferedImage doInBackground() {
                    try {
                        URL url = new URL(imageUrls.get(prefetchIndex));
                        return ImageIO.read(url);
                    } catch (Exception e) {
                        Logger_.error("Failed to prefetch previous image: " + e.getMessage());
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        BufferedImage img = get();
                        if (img != null) {
                            loadedImages.put(prefetchIndex, img);
                            Logger_.info("Prefetched previous image: " + prefetchIndex + " stored.");
                        }
                    } catch (Exception e) {
                        Logger_.error("Error storing prefetched previous image: " + e.getMessage());
                    }
                }
            };

            worker.execute();
        }
    }

    private void updateImage() {
        try {
            BufferedImage img;
            if (loadedImages.containsKey(currentIndex)) {
                Logger_.info("Using prefetched image: " + currentIndex);
                img = loadedImages.get(currentIndex);
            }
            // On first start, and if the prefetch fails for some reason
            else {
                Logger_.info("Downloading image: " + currentIndex);
                URL url = new URL(imageUrls.get(currentIndex));
                img = ImageIO.read(url);
                // Store images so we do not need to wait for fetch again as they are quite big
                loadedImages.put(currentIndex, img);
            }

            if (img == null) {
                Logger_.error("Failed to load image.");
                imageLabel.setText("Failed to load image.");
                return;
            }

            // Make sure the image fits within the frame
            int maxWidth = 800;
            int maxHeight = 500;

            // Get original image dimensions
            int imgWidth = img.getWidth();
            int imgHeight = img.getHeight();

            // Do some magic to calculate the scaling factor to maintain aspect ratio
            double widthRatio = (double) maxWidth / imgWidth;
            double heightRatio = (double) maxHeight / imgHeight;
            double scaleFactor = Math.min(widthRatio, heightRatio);

            int newWidth = (int) (imgWidth * scaleFactor);
            int newHeight = (int) (imgHeight * scaleFactor);

            // Resize the image
            Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(scaledIcon);

            // Remove error if set
            imageLabel.setText("");
            revalidate();
            repaint();
        } catch (Exception e) {
            Logger_.error(e.getMessage());
            imageLabel.setText("Failed to load image");
        }
    }
}
