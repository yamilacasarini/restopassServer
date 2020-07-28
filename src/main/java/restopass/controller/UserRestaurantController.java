package restopass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import restopass.dto.UserRestaurant;
import restopass.dto.request.UserLoginRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.service.UserRestaurantService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users/restaurants")
public class UserRestaurantController {

    @Autowired
    UserRestaurantService userRestaurantService;

    private String USER_ID_ATTR = "userId";

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void createUser(@RequestBody UserRestaurant user) {
        this.userRestaurantService.createUserRestaurant(user);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public UserLoginResponse<UserRestaurant> userLogin(@RequestBody UserLoginRequest user) {
        return userRestaurantService.loginRestaurantUser(user);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void deleteUser(HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        this.userRestaurantService.deleteUserRestaurant(userId);
    }


}
