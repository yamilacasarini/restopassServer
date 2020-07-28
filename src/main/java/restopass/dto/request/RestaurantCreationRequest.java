package restopass.dto.request;

import restopass.dto.RestaurantHours;

import java.util.List;

public class RestaurantCreationRequest {

    private String name;
    private String img;
    private String address;
    private double latitude;
    private double longitude;
    private List<RestaurantHours> timeTable;
    private List<String> tags;
    private List<DishRequest> dishes;

    public List<DishRequest> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishRequest> dishes) {
        this.dishes = dishes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<RestaurantHours> getTimeTable() {
        return timeTable;
    }

    public void setTimeTable(List<RestaurantHours> timeTable) {
        this.timeTable = timeTable;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
