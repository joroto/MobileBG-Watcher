import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MobileBGWatcherFrame mobileBGWatcherFrame = new MobileBGWatcherFrame();
            mobileBGWatcherFrame.setVisible(true);
        });
    }
}
