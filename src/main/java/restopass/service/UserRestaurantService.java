package restopass.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import restopass.dto.Restaurant;
import restopass.dto.UserRestaurant;
import restopass.dto.request.UserLoginRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.dto.response.UserRestaurantResponse;
import restopass.exception.UserAlreadyExistsException;
import restopass.mongo.UserRestaurantRepository;
import restopass.service.GenericUserService;


@Service
public class UserRestaurantService extends GenericUserService {

    private final String USER_ID_FIELD = "email";
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

        this.userRestaurantRepository.save(userRestaurant);
    }

    public UserRestaurantResponse loginRestaurantUser(UserLoginRequest userLoginRequest) {
        UserLoginResponse<UserRestaurant> user = this.loginUser(userLoginRequest);
        return new UserRestaurantResponse(user, this.restaurantService.findById(user.getUser().getRestaurantId()));
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
