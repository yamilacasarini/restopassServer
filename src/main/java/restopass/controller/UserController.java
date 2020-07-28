package restopass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import restopass.dto.User;
import restopass.dto.request.UserCreationRequest;
import restopass.dto.request.UserLoginGoogleRequest;
import restopass.dto.request.UserLoginRequest;
import restopass.dto.request.UserUpdateRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.service.UserService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    private String USER_ID_ATTR = "userId";

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public  UserLoginResponse<User> userLogin(@RequestBody UserLoginRequest user) {
        return userService.loginUser(user);
    }

    @RequestMapping(value = "/login/google", method = RequestMethod.POST)
    public  UserLoginResponse<User> userLoginGoogle(@RequestBody UserLoginGoogleRequest user) {
        return userService.loginGoogleUser(user);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public  UserLoginResponse<User> createUser(@RequestBody UserCreationRequest user) {
        return this.userService.createUser(user);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public User getUser(HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        return this.userService.findById(userId);
    }

    @RequestMapping(value = "", method = RequestMethod.PATCH)
    public void updateUser(HttpServletRequest request, @RequestBody UserUpdateRequest userUpdateRequest) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        this.userService.updateUserInfo(userUpdateRequest, userId);
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public UserLoginResponse<User> refreshToken(HttpServletRequest request) {
        return this.userService.refreshToken(request);
    }

    @RequestMapping(value = "/favorite/{restaurantId}", method = RequestMethod.POST)
    public void addRestaurantToFavorites(HttpServletRequest request, @PathVariable String restaurantId) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        this.userService.addNewRestaurantFavorite(restaurantId, userId);
    }

    @RequestMapping(value = "/unfavorite/{restaurantId}", method = RequestMethod.POST)
    public void removeRestaurantFromFavorites(HttpServletRequest request, @PathVariable String restaurantId) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        this.userService.removeRestaurantFavorite(restaurantId, userId);
    }

    @RequestMapping(value = "/check/{userId}/{baseMembership}", method = RequestMethod.POST)
    public User checkCanAddToReservation(@PathVariable String userId, @PathVariable Integer baseMembership) {
        return this.userService.checkCanAddToReservation(userId, baseMembership);
    }

    @RequestMapping(value = "/emails", method = RequestMethod.PATCH)
    public void addToConfirmEmail(HttpServletRequest request, @RequestBody UserUpdateRequest userUpdateRequest) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        this.userService.updateUserInfo(userUpdateRequest, userId);
    }

    @RequestMapping(value = "/emails/{email}", method = RequestMethod.DELETE)
    public void removeEmail(HttpServletRequest request, @PathVariable String email) {
        String userId = request.getAttribute(USER_ID_ATTR).toString();
        this.userService.removeEmail(email, userId);
    }

    @RequestMapping(value = "/emails/confirm/{email}/{userId}", method = RequestMethod.GET)
    public ModelAndView confirmEmail(@PathVariable String email, @PathVariable  String userId) {
        this.userService.confirmEmail(email, userId);

        ModelAndView modelAndView = new ModelAndView();

        User user = this.userService.findById(userId);
        modelAndView.addObject("name", user.getName());
        modelAndView.addObject("email", email);
        modelAndView.setViewName("/confirmEmail/confirm-email-success");

        return modelAndView;
    }
}
