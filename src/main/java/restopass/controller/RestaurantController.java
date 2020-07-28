package restopass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import restopass.dto.Restaurant;
import restopass.dto.RestaurantConfig;
import restopass.dto.request.DishRequest;
import restopass.dto.request.RestaurantCreationRequest;
import restopass.dto.request.RestaurantTagsRequest;
import restopass.dto.request.ScoreRequest;
import restopass.dto.response.RestaurantTagsResponse;
import restopass.service.FirebaseService;
import restopass.service.RestaurantService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    @Autowired
    RestaurantService restaurantService;
    @Autowired
    FirebaseService firebaseService;

    private String USER_ID_ATTR = "userId";

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void createRestaurant(@RequestBody RestaurantCreationRequest restaurant) {
        this.restaurantService.createRestaurant(restaurant);
    }

    @RequestMapping(value = "/{restaurantId}", method = RequestMethod.GET)
    public Restaurant findById(@PathVariable String restaurantId) {
        return this.restaurantService.findById(restaurantId);
    }

    @RequestMapping(value = "/favorites", method = RequestMethod.GET)
    public Set<Restaurant> getAllFavoritesByUser(HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        return this.restaurantService.findAllFavoritesByUser(userId);
    }

    @RequestMapping(value = "config", method = RequestMethod.POST)
    public void createRestaurantConfig(@RequestBody RestaurantConfig restaurant) {
        this.restaurantService.createRestaurantConfig(restaurant);
    }

    @RequestMapping(value = "config/{restaurantId}", method = RequestMethod.GET)
    public RestaurantConfig getRestaurantConfig(@PathVariable String restaurantId) {
        return this.restaurantService.buildRestaurantConfig(restaurantId);
    }
    @RequestMapping(value = "/dishes/{restaurantId}", method = RequestMethod.PATCH)
    public void addPlate(@RequestBody DishRequest dish, @PathVariable String restaurantId) {
        this.restaurantService.addDish(dish, restaurantId);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public List<Restaurant> getRestaurantByTags(@RequestBody RestaurantTagsRequest request) {
        return this.restaurantService.getByTags(request.getLat(), request.getLng(), request.getTags(), request.getTopMembership(), request.getFreeText());
    }

    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public RestaurantTagsResponse getRestaurantsTags() {
        return this.restaurantService.getRestaurantsTags();
    }

    @RequestMapping(value = "/score", method = RequestMethod.POST)
    public void scoreRestaurantAndDish(@RequestBody ScoreRequest scoreRequest) {
        this.restaurantService.scoreRestaurantAndDish(scoreRequest);
    }

    @RequestMapping(value = "/timetable/{restaurantId}", method = RequestMethod.PATCH)
    public void updateTimeTable(@PathVariable String restaurantId, @RequestBody Restaurant restaurant) {
        this.restaurantService.updateTimeTable(restaurantId, restaurant);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void test() {
        firebaseService.sendScoreNotification(Arrays.asList("prueba@prueba.com"), "b200dcd7-dabd-4df2-9305-edaf90dad56b", "La Causa Nikkei");
    }

}
