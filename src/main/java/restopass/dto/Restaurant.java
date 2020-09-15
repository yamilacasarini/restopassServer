package restopass.dto;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "restaurants")
public class Restaurant {

    private String restaurantId;
    private String name;
    private String img;
    private String address;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;
    private List<RestaurantHours> timeTable;
    private List<String> tags;
    private List<Dish> dishes;
    private Float stars = 0f;
    private Integer countStars = 0;
    private Integer hoursToCancel;

    public Restaurant() {
    }


    public Integer getCountStars() {
        return countStars;
    }

    public void setCountStars(Integer countStars) {
        this.countStars = countStars;
    }

    public Integer getHoursToCancel() {
        return hoursToCancel;
    }

    public void setHoursToCancel(Integer hoursToCancel) {
        this.hoursToCancel = hoursToCancel;
    }

    public Float getStars() {
        return stars;
    }

    public void setStars(Float stars) {
        this.stars = stars;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public GeoJsonPoint getLocation() {
        return location;
    }

    public void setLocation(GeoJsonPoint location) {
        this.location = location;
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

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public void setAverageStars() {
        Integer countStars = getCountStars();
        if (countStars != 0) {
            this.setStars(getStars() / countStars);
        }
    }
}
