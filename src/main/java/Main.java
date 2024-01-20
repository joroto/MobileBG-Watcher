import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.2");
        SwingUtilities.invokeLater(() -> {
            CarWatcher carInfoViewer2 = new CarWatcher();
            carInfoViewer2.setVisible(true);
        });
    }
}
