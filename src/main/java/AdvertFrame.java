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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdvertFrame extends JFrame {
    private ExecutorService executorService; // Thread pool for prefetching
    List<String> imageUrls;
    Map<Integer, BufferedImage> loadedImages;
    private JLabel imageLabel;
    private int currentIndex = 0;
    JPanel infoPanel;

    public AdvertFrame(Advert advert) {
        imageUrls = advert.getImageUrls();
        loadedImages = new HashMap<>();
        executorService = Executors.newFixedThreadPool(2); // You can adjust the pool size based on the number of images you want to preload
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
                advert.addToFavourites();
                favouriteAdvertButton.setBackground(new Color(198, 174, 61));
            } else {
                advert.removeFromFavourites();
                favouriteAdvertButton.setBackground(background);
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
            BufferedImage image;
            // Should always be loaded, else is just in case
            if (loadedImages.containsKey(currentIndex)) {
                image = loadedImages.get(currentIndex);
            } else {
                Logger_.error("Downloading image for zoom in, check why it is not already loaded!");
                image = ImageIO.read(new URL(imageUrls.get(currentIndex)));
            }
            new ZoomedFrame(image);
        } catch (Exception ex) {
            Logger_.error(ex.getMessage());
        }
    }

    private void addSpace() {
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void prefetchImages() {
        // Calculate previous and next indices
        int previousIndex = (currentIndex - 1 + imageUrls.size()) % imageUrls.size();
        int nextIndex = (currentIndex + 1) % imageUrls.size();

        // Fetch both previous and next images in parallel
        executorService.submit(() -> prefetchImage(previousIndex));
        executorService.submit(() -> prefetchImage(nextIndex));
    }

    private void prefetchImage(int index) {
        if (!loadedImages.containsKey(index)) {
            try {
                URL url = new URL(imageUrls.get(index));
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    loadedImages.put(index, img);
                    Logger_.info("Prefetched image " + index);
                }
            } catch (Exception e) {
                Logger_.error("Failed to prefetch image " + index);
            }
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
            Image scaledImage = scaleImage(img, maxWidth, maxHeight);

            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(scaledIcon);

            // Remove error if set
            imageLabel.setText("");
            revalidate();
            repaint();
            prefetchImages();
        } catch (Exception e) {
            Logger_.error("Failed to load image");
            Logger_.error(e.getMessage());
            imageLabel.setText("Failed to load image");
        }
    }

    private Image scaleImage(BufferedImage img, int maxWidth, int maxHeight) {
        // Do some magic to calculate the scaling factor to maintain aspect ratio
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        double widthRatio = (double) maxWidth / imgWidth;
        double heightRatio = (double) maxHeight / imgHeight;
        double scaleFactor = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (imgWidth * scaleFactor);
        int newHeight = (int) (imgHeight * scaleFactor);

        return img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }
}