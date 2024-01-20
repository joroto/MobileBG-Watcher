import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.2");
        SwingUtilities.invokeLater(() -> {
            MobileBGWatcher mobileBGWatcher = new MobileBGWatcher();
            mobileBGWatcher.setVisible(true);
        });
    }
}
