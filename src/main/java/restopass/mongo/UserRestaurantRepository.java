package restopass.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import restopass.dto.UserRestaurant;

@Service
public interface UserRestaurantRepository extends MongoRepository<UserRestaurant, String> {
}
