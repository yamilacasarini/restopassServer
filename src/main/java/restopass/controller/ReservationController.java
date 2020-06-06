package restopass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import restopass.dto.Reservation;
import restopass.dto.User;
import restopass.dto.response.ReservationResponse;
import restopass.exception.NoMoreVisitsException;
import restopass.exception.ReservationAlreadyConfirmedException;
import restopass.exception.ReservationCanceledException;
import restopass.service.ReservationService;
import restopass.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserService userService;

    private String USER_ID = "userId";

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void createReservation(@RequestBody Reservation reservation, HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        this.reservationService.createReservation(reservation, userId);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<ReservationResponse> getReservationByUser(HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        return this.reservationService.getReservationsForUser(userId);
    }

    @RequestMapping(value = "/cancel/{reservationId}", method = RequestMethod.PATCH)
    public List<ReservationResponse> cancelReservation(@PathVariable String reservationId, HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        return this.reservationService.cancelReservation(reservationId, userId);
    }


    @RequestMapping(value = "/done/{reservationId}", method = RequestMethod.GET)
    public ModelAndView doneReservation(@PathVariable String reservationId,
                                @RequestParam(value = "restaurant_id") String restaurantId,
                                @RequestParam(value = "user_id") String userId) {
        this.reservationService.doneReservation(reservationId, restaurantId, userId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/reservation/done-reservation");
        return modelAndView;
    }

    @RequestMapping(value = "/confirm/{reservationId}/{userId}", method = RequestMethod.GET)
    public ModelAndView confirmReservation(@PathVariable String reservationId, @PathVariable String userId) {
        ModelAndView modelAndView = new ModelAndView();
        User user;

        try {
            this.reservationService.confirmReservation(reservationId, userId);
            user = this.userService.findById(userId);
            modelAndView.addObject("name", user.getName());
            modelAndView.addObject("email", user.getEmail());
            modelAndView.setViewName("/reservation/confirm-reservation");
        } catch (NoMoreVisitsException e) {
            modelAndView.addObject("msg", e.getMessage());
            modelAndView.setViewName("/reservation/no-more-visits");
        } catch (ReservationAlreadyConfirmedException e) {
            user = this.userService.findById(userId);
            modelAndView.addObject("name", user.getName());
            modelAndView.setViewName("/reservation/already-confirmed-reservation");
        } catch (ReservationCanceledException e) {
            modelAndView.setViewName("/reservation/canceled-reservation");
        } catch (Exception e) {
            modelAndView.setViewName("/reservation/error-reservation");
        }

        return modelAndView;
    }




}
