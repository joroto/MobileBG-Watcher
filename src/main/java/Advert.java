import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Advert {
    boolean isFavourite;
    private String advertURL;
    private List<String> imageUrls;
    private String advertTitle;
    private String carLocation;
    private String carPrice;
    private String advertPhone;
    //    String priceHistory;
    private Map<String, String> mainCarParams;
    private String advertStats;
    private String advertDescription;
    private Long advertNumber;
    private BufferedImage mainImage;

    public Advert(String imageUrl, String title, String url, String price, Long advNumber, boolean isFavourite) {
        this.advertTitle = title;
        this.advertURL = url;
        this.carPrice = price;
        this.advertNumber = advNumber;
        this.mainImage = readImageFromURL(imageUrl);
        this.isFavourite = isFavourite;
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

    public Long getAdvertNumber() {
        return advertNumber;
    }

    public void setAdvertNumber(Long advertNumber) {
        this.advertNumber = advertNumber;
    }

    public BufferedImage getMainImage() {
        return mainImage;
    }

    public void setMainImage(BufferedImage mainImage) {
        this.mainImage = mainImage;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAdvertURL() {
        return advertURL;
    }

    public void setAdvertURL(String advertURL) {
        this.advertURL = advertURL;
    }

    public String getCarPrice() {
        return carPrice;
    }

    public void setCarPrice(String carPrice) {
        this.carPrice = carPrice;
    }

    public String getAdvertDescription() {
        return advertDescription;
    }

    public void setAdvertDescription(String advertDescription) {
        this.advertDescription = advertDescription;
    }

    public String getAdvertTitle() {
        return advertTitle;
    }

    public void setAdvertTitle(String advertTitle) {
        this.advertTitle = advertTitle;
    }

    public String getCarLocation() {
        return carLocation;
    }

    public void setCarLocation(String carLocation) {
        this.carLocation = carLocation;
    }

    public String getAdvertPhone() {
        return advertPhone;
    }

    public void setAdvertPhone(String advertPhone) {
        this.advertPhone = advertPhone;
    }

//    public String getPriceHistory() {
//        return priceHistory;
//    }
//
//    public void setPriceHistory(String priceHistory) {
//        this.priceHistory = priceHistory;
//    }

    public Map<String, String> getMainCarParams() {
        return mainCarParams;
    }

    public void setMainCarParams(Map<String, String> mainCarParams) {
        this.mainCarParams = mainCarParams;
    }

    public String getAdvertStats() {
        return advertStats;
    }

    public void setAdvertStats(String advertStats) {
        this.advertStats = advertStats;
    }
}
