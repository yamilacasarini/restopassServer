package restopass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import restopass.dto.User;
import restopass.dto.request.CreateReservationRequest;
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
    public void createReservation(@RequestBody CreateReservationRequest reservation, HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        this.reservationService.createReservation(reservation, userId);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<ReservationResponse> getReservationByUser(HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        return this.reservationService.getReservationsForUser(userId);
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public List<ReservationResponse> getHistoryByUser(HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        return this.reservationService.getReservationsHistoryForUser(userId);
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
         return this.reservationService.doneReservation(reservationId, restaurantId, userId);
    }

    @RequestMapping(value = "/confirm/{reservationId}", method = RequestMethod.PATCH)
    public List<ReservationResponse> confirmReservationFromMobile(@PathVariable String reservationId, HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        this.reservationService.confirmReservation(reservationId, userId);
        return this.reservationService.getReservationsForUser(userId);
    }

    @RequestMapping(value = "/reject/{reservationId}", method = RequestMethod.PATCH)
    public List<ReservationResponse> rejectReservationFromMobile(@PathVariable String reservationId, HttpServletRequest request) {
        String userId = request.getAttribute(USER_ID).toString();
        this.reservationService.rejectReservation(reservationId, userId);
        return this.reservationService.getReservationsForUser(userId);
    }

    @RequestMapping(value = "/confirm/{reservationId}/{email}", method = RequestMethod.GET)
    public ModelAndView confirmReservation(@PathVariable String reservationId, @PathVariable String email) {
        ModelAndView modelAndView = new ModelAndView();
        User user;

        try {
            this.reservationService.confirmReservation(reservationId, email);
            user = this.userService.findById(email);
            modelAndView.addObject("name", user.getName());
            modelAndView.addObject("email", email);
            modelAndView.setViewName("/reservation/confirm-reservation");
        } catch (NoMoreVisitsException e) {
            modelAndView.addObject("msg", e.getMessage());
            modelAndView.setViewName("/reservation/no-more-visits");
        } catch (ReservationAlreadyConfirmedException e) {
            user = this.userService.findById(email);
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
