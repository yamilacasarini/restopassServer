package restopass.dto.response;

import restopass.dto.Restaurant;
import restopass.dto.UserRestaurant;

public class UserRestaurantResponse extends UserLoginResponse<UserRestaurant> {
    private Restaurant restaurant;

    public UserRestaurantResponse(UserLoginResponse<UserRestaurant> user, Restaurant restaurant) {
        super(user.getxAuthToken(), user.getxRefreshToken(), user.getUser());
        this.restaurant = restaurant;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
