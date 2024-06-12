import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class ZoomedFrame extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage zoomedImage;
    private JLabel zoomedLabel;
    private int zoomFactor = 1;
    private static final int MAX_ZOOM_FACTOR = 3;
    private Point initialClick;
    private Point imagePosition = new Point(0, 0);

    public ZoomedFrame(URL imageUrl) {
        try {
            originalImage = ImageIO.read(imageUrl);
            zoomedImage = zoomImage(originalImage, zoomFactor);
            initializeUI();
        } catch (IOException ex) {
            Logger_.error(ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setTitle("Zoomable Image");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        zoomedLabel = new JLabel(new ImageIcon(zoomedImage));
        zoomedLabel.setPreferredSize(new Dimension(zoomedImage.getWidth(), zoomedImage.getHeight()));
        getContentPane().add(zoomedLabel, BorderLayout.CENTER);

        pack();
        setVisible(true);

        zoomedLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                zoomedLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            public void mouseReleased(MouseEvent e) {
                zoomedLabel.setCursor(Cursor.getDefaultCursor());
            }
        });

        zoomedLabel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int deltaX = e.getX() - initialClick.x;
                int deltaY = e.getY() - initialClick.y;
                imagePosition.translate(deltaX, deltaY);
                updateZoomedLabel();
                initialClick = e.getPoint();
            }
        });

        zoomedLabel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) {
                    if (zoomFactor < MAX_ZOOM_FACTOR) {
                        zoomFactor++;
                    }
                } else {
                    if (zoomFactor > 1) {
                        zoomFactor--;
                    }
                }

                zoomedImage = zoomImage(originalImage, zoomFactor);
                updateZoomedLabel();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        setFocusable(true);
        requestFocus();
    }

    private void updateZoomedLabel() {
        int x = imagePosition.x * zoomFactor;
        int y = imagePosition.y * zoomFactor;
        int width = zoomedLabel.getWidth();
        int height = zoomedLabel.getHeight();

        x = Math.min(Math.max(x, 0), zoomedImage.getWidth() - width);
        y = Math.min(Math.max(y, 0), zoomedImage.getHeight() - height);

        BufferedImage subImage = zoomedImage.getSubimage(x, y, width, height);
        zoomedLabel.setIcon(new ImageIcon(subImage));
    }

    private BufferedImage zoomImage(BufferedImage originalImage, int zoomFactor) {
        int newWidth = originalImage.getWidth() * zoomFactor;
        int newHeight = originalImage.getHeight() * zoomFactor;

        BufferedImage zoomedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = zoomedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2.dispose();

        return zoomedImage;
    }
}
