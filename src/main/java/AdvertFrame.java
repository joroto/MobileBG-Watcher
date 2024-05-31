import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class AdvertFrame extends JFrame {
    List<String> imageUrls;
    private JLabel imageLabel;
    private int currentIndex = 0;
    JPanel infoPanel;

    public AdvertFrame(Advert advert) {
        imageUrls = advert.getImageUrls();
        setTitle("Image Slider");
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
        Font labelFont = new Font("Arial", Font.BOLD, 12);

        JLabel jLabelCarLocation = new JLabel(advert.carLocation);
        jLabelCarLocation.setFont(labelFont);
        infoPanel.add(jLabelCarLocation);
        addSpace();

        Map<String, String> mainCarParams = advert.mainCarParams;

        for (String s : mainCarParams.keySet()) {
            JLabel jLabelCarParams = new JLabel(advert.mainCarParams.get(s));
            jLabelCarParams.setFont(labelFont);
            infoPanel.add(jLabelCarParams);
            addSpace();
        }

        JLabel jLabelAdvertPhone = new JLabel(advert.advertPhone);
        jLabelAdvertPhone.setFont(labelFont);
        infoPanel.add(jLabelAdvertPhone);
        addSpace();

        JLabel jLabelAdvertStats = new JLabel("<html>" + advert.advertStats);
        jLabelAdvertStats.setFont(labelFont);
        infoPanel.add(jLabelAdvertStats);
        addSpace();

        JLabel jLabelAdvertDescription = new JLabel("<html>" + advert.advertDescription);
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
        }

        favouriteAdvertButton.addActionListener(e -> {
            if (!advert.isFavourite) {
                advert.setFavourite(true);
                favouriteAdvertButton.setBackground(new Color(198, 174, 61));
            } else {
                advert.setFavourite(false);
                favouriteAdvertButton.setBackground(background);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(openAdvertButton);
        buttonPanel.add(favouriteAdvertButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addSpace(){
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void updateImage() {
        try {
            URL url = new URL(imageUrls.get(currentIndex));
            ImageIcon imageIcon = new ImageIcon(url);
            imageLabel.setIcon(imageIcon);
        } catch (Exception e) {
            Logger_.error(e.getMessage());
            imageLabel.setText("Failed to load image");
        }
    }
}
