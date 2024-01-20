import java.awt.image.BufferedImage;

public class Car {
    private BufferedImage image;
    private String title;
    private String url;
    private String price;
    private long advN;

    public Car(BufferedImage image, String title, String url, String price, Long advParam) {
        this.image = image;
        this.title = title;
        this.url = url;
        this.price = price;
        this.advN = advParam;
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