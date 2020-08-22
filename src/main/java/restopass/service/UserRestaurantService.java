package restopass.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import restopass.dto.Dish;
import restopass.dto.Restaurant;
import restopass.dto.UserRestaurant;
import restopass.dto.request.UserLoginRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.exception.UserAlreadyExistsException;
import restopass.mongo.UserRestaurantRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;


@Service
public class UserRestaurantService extends GenericUserService {

    private final String USER_ID_FIELD = "email";
    private final String RESTOPASS_MAIL = "@restopass.com";
    @Autowired
    private UserRestaurantRepository userRestaurantRepository;
    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private MongoTemplate mongoTemplate;

    public void createUserRestaurant(UserRestaurant userRestaurant) {
        if (this.findById(userRestaurant.getEmail()) != null) {
            throw new UserAlreadyExistsException();
        }

        Restaurant restaurant = this.restaurantService.findById(userRestaurant.getRestaurantId());
        userRestaurant.setEmail(restaurant.getName().replaceAll(" ","").toLowerCase() + RESTOPASS_MAIL);
        this.userRestaurantRepository.save(userRestaurant);
    }

    public UserLoginResponse<UserRestaurant> loginRestaurantUser(UserLoginRequest userLoginRequest) {
        UserLoginResponse<UserRestaurant> user = this.loginUser(userLoginRequest);
        Restaurant restaurant = this.restaurantService.findById(user.getUser().getRestaurantId());
        restaurant.getDishes().sort(Comparator.comparing(Dish::getBaseMembershipName));
        restaurant.getDishes().forEach(d -> d.setStars(d.getStars() / d.getCountStars()));
        restaurant.setStars(restaurant.getStars() / restaurant.getCountStars());
        user.getUser().setRestaurant(restaurant);
        return user;
    }

    public UserLoginResponse<UserRestaurant> refreshRestaurantToken(HttpServletRequest req) {
        UserLoginResponse<UserRestaurant> user = this.refreshToken(req);
        user.getUser().setRestaurant(this.restaurantService.findById(user.getUser().getRestaurantId()));
        return user;
    }

    public void deleteUserRestaurant(String userId) {
        UserRestaurant userRestaurant = this.findById(userId);
        this.userRestaurantRepository.delete(userRestaurant);
    }

    public UserRestaurant findById(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(USER_ID_FIELD).is(userId));

        return this.mongoTemplate.findOne(query, UserRestaurant.class);
    }

    public UserRestaurant findByUserAndPass(Query query) {
        return this.mongoTemplate.findOne(query, UserRestaurant.class);
    }

}
