package restopass.dto.firebase;

public class ScorePushNotifData extends SimpleNotifData {
    private String type;
    private String restaurantId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
