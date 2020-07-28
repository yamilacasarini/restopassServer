package restopass.dto;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users_restaurant")
public class UserRestaurant extends GenericUser {

    private String restaurantId;

    public UserRestaurant(String email, String password, String restaurantId) {
        super(email, password);
        this.restaurantId = restaurantId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
