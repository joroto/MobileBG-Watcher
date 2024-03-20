import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Car {
    private BufferedImage image;
    private String title;
    private String url;
    private String price;
    private long advN;

    public Car(String imageUrl, String title, String url, String price, Long advParam) {
        this.title = title;
        this.url = url;
        this.price = price;
        this.advN = advParam;
        this.image = readImageFromURL(imageUrl);
    }

    private BufferedImage readImageFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            Logger_.error("Failed to read image from URL: " + imageUrl);
            return null;
        }
    }

    public long getAdvN() {
        return advN;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getPrice() {
        return price;
    }
}