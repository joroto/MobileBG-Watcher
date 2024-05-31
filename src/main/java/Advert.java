import java.util.List;
import java.util.Map;

public class Advert {
    boolean isFavourite;
    String advertURL;
    List<String> imageUrls;
    String advertTitle;
    String carLocation;
    String advertPhone;
    //    String priceHistory;
    Map<String, String> mainCarParams;
    String advertStats;
    String advertDescription;

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

    public Advert(List<String> imageUrls, String advertTitle, String carLocation, String advertPhone /*,String priceHistory */, Map<String, String> mainCarParams, String advertStats, String advertURL, String advertDescription) {
        this.imageUrls = imageUrls;
        this.advertTitle = advertTitle;
        this.carLocation = carLocation;
        this.advertPhone = advertPhone;
//        this.priceHistory = priceHistory;
        this.mainCarParams = mainCarParams;
        this.advertStats = advertStats;
        this.advertURL = advertURL;
        this.advertDescription = advertDescription;
        this.isFavourite = false;
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
