package restopass.service;

import com.google.api.gax.tracing.TracedOperationCallable;
import io.jsonwebtoken.lang.Strings;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import restopass.dto.*;
import restopass.dto.request.CreateReservationRequest;
import restopass.dto.response.DoneReservationResponse;
import restopass.dto.response.ReservationResponse;
import restopass.dto.response.UserReservation;
import restopass.exception.*;
import restopass.mongo.MembershipRepository;
import restopass.mongo.ReservationRepository;
import restopass.utils.EmailSender;
import restopass.utils.QRHelper;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private MongoTemplate mongoTemplate;
    private ReservationRepository reservationRepository;
    private MembershipRepository membershipRepository;
    private UserService userService;
    private FirebaseService firebaseService;
    private String OWNER_USER_ID = "ownerUser";
    private String RESERVATION_ID = "reservationId";
    private String RESERVATION_STATE = "state";
    private String CONFIRMED_USERS = "confirmedUsers";
    private String TO_CONFIRM_USERS = "toConfirmUsers";
    private String RESERVATION_COLLECTION = "reservations";

    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private UserRestaurantService userRestaurantService;

    @Autowired
    public ReservationService(MongoTemplate mongoTemplate, ReservationRepository reservationRepository, UserService userService, FirebaseService firebaseService,
                              MembershipRepository membershipRepository) {
        this.mongoTemplate = mongoTemplate;
        this.reservationRepository = reservationRepository;
        this.membershipRepository = membershipRepository;
        this.userService = userService;
        this.firebaseService = firebaseService;
    }

    public void createReservation(CreateReservationRequest reservationRequest, String userId) {
        String reservationId =  RandomStringUtils.randomNumeric(10);
        Reservation reservation = new Reservation();
        reservation.setRestaurantId(reservationRequest.getRestaurantId());
        reservation.setToConfirmUsers(reservationRequest.getToConfirmUsers());
        reservation.setDate(LocalDateTime.parse(reservationRequest.getDate()));
        reservation.setDinners(reservationRequest.getDinners());
        reservation.setReservationId(reservationId);
        reservation.setOwnerUser(userId);

        RestaurantConfig restaurantConfig = this.restaurantService.findConfigurationByRestaurantId(reservation.getRestaurantId());
        List<RestaurantSlot> slots = this.restaurantService.decrementTableInSlot(restaurantConfig, reservation.getDate());
        this.restaurantService.updateSlotsInDB(reservation.getRestaurantId(), slots);

        reservation.setQrBase64(QRHelper.createQRBase64(reservationId, userId));

        this.sendConfirmBookingEmail(reservation, userId);
        if (!CollectionUtils.isEmpty(reservation.getToConfirmUsers())) {
            this.sendNewBookingEmail(reservation);
            this.sendNewBookingNotif(reservation);
        }

        this.userService.decrementUserVisits(userId);
        this.reservationRepository.save(reservation);
    }

    private void sendNewBookingNotif(Reservation reservation) {
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());
        this.firebaseService.sendNewInvitationNotification(reservation.getToConfirmUsers(),
                reservation.getReservationId(), reservation.getOwnerUser(), restaurant.getName(),
                this.generateHumanDate(reservation.getDate()));
    }

    private void sendConfirmBookingEmail(Reservation reservation, String userId) {
        User user = this.userService.findById(reservation.getOwnerUser());
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());

        Boolean isOwner = userId.equals(user.getEmail());

        HashMap<String, Object> modelEmail = new HashMap<>();

        if(!isOwner) {
            User userGuest = this.userService.findById(userId);
            modelEmail.put("userName", userGuest.getName());
        } else {
            modelEmail.put("userName", user.getName());
        }

        modelEmail.put("restaurantName", restaurant.getName());
        modelEmail.put("totalDiners", reservation.getDinners());
        modelEmail.put("date", this.generateHumanDate(reservation.getDate()));
        modelEmail.put("restaurantAddress", restaurant.getAddress());
        modelEmail.put("qrCode", reservation.getQrBase64());
        modelEmail.put("cancelDate", this.generateHumanDate(reservation.getDate().minusHours(restaurant.getHoursToCancel())));
        modelEmail.put("isOwner", isOwner);

        EmailModel emailModel = new EmailModel();
        emailModel.setMailTempate("confirm_booking.ftl");
        emailModel.setSubject("Tu reserva ha sido confirmada");
        emailModel.setModel(modelEmail);

        if (isOwner) {
            this.sendMultiEmail(user, emailModel);
        } else {
            emailModel.setEmailTo(userId);
            EmailSender.sendEmail(emailModel);
        }

    }

    private void sendMultiEmail(User user, EmailModel emailModel) {
        emailModel.setEmailTo(user.getEmail());
        EmailSender.sendEmail(emailModel);
        user.getSecondaryEmails().forEach(email -> {
            emailModel.setEmailTo(email);
            EmailSender.sendEmail(emailModel);
        });
    }

    private void sendNewBookingEmail(Reservation reservation) {
        User ownerUser = this.userService.findById(reservation.getOwnerUser());
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());

        HashMap<String, Object> modelEmail = new HashMap<>();
        modelEmail.put("ownerUser", ownerUser.getName() + " " + ownerUser.getLastName());
        modelEmail.put("restaurantName", restaurant.getName());
        modelEmail.put("totalDiners", reservation.getDinners());
        modelEmail.put("date", this.generateHumanDate(reservation.getDate()));
        modelEmail.put("restaurantAddress", restaurant.getAddress());

        EmailModel emailModel = new EmailModel();
        emailModel.setMailTempate("new_booking.html");
        emailModel.setSubject("Parece que tienes una nueva reserva");
        emailModel.setModel(modelEmail);

        reservation.getToConfirmUsers().forEach(userEmail -> {
            modelEmail.put("joinUrl", this.buildJoinUrl(reservation.getReservationId(), userEmail));

            User user = userService.findById(userEmail);
            this.sendMultiEmail(user, emailModel);
        });
    }

    public List<ReservationResponse> getReservationsForUser(String userId) {
        Query query = new Query();

        Criteria orCriteria = new Criteria();
        query.addCriteria(Criteria.where(RESERVATION_STATE).is(ReservationState.CONFIRMED));

        orCriteria.orOperator(
                Criteria.where(OWNER_USER_ID).is(userId),
                Criteria.where(CONFIRMED_USERS).in(userId),
                Criteria.where(TO_CONFIRM_USERS).in(userId));

        query.addCriteria(orCriteria);

        List<Reservation> reservations = this.mongoTemplate.find(query, Reservation.class);

        return this.orderAndMapReservations(reservations, userId);
    }

    public List<ReservationResponse> getReservationsHistoryForUser(String userId) {
        Query query = new Query();

        Criteria orCriteria = new Criteria();
        query.addCriteria(Criteria.where(RESERVATION_STATE).ne(ReservationState.CONFIRMED));

        orCriteria.orOperator(
                Criteria.where(OWNER_USER_ID).is(userId),
                Criteria.where(CONFIRMED_USERS).in(userId));

        query.addCriteria(orCriteria);

        List<Reservation> reservations = this.mongoTemplate.find(query, Reservation.class);

        return this.orderAndMapReservations(reservations, userId);
    }

    private List<ReservationResponse> orderAndMapReservations(List<Reservation> reservations, String userId) {
        reservations.sort(Comparator.comparing(Reservation::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return reservations.stream().map(r -> this.mapReservationToResponse(r, userId)).collect(Collectors.toList());
    }

    public List<ReservationResponse> cancelReservation(String reservationId, String userId) {
        List<ReservationResponse> reservations = this.getReservationsForUser(userId);
        ReservationResponse reservation = reservations.stream().filter(r -> r.getReservationId().equalsIgnoreCase(reservationId)).findFirst().get();
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());

        if (reservation.getDate().minusHours(restaurant.getHoursToCancel()).isAfter(LocalDateTime.now())) {
            throw new ReservationCancelTimeExpiredException();
        }

        this.updateReservationState(reservationId, ReservationState.CANCELED);
        this.userService.incrementUserVisits(userId);

        if (reservation.getConfirmedUsers() != null) {
            this.firebaseService.sendCancelReservationNotification(
                    reservation.getConfirmedUsers().stream().map(UserReservation::getUserId).collect(Collectors.toList()),
                    reservationId, reservation.getOwnerUser().getName() + " " + reservation.getOwnerUser().getLastName(),
                    reservation.getRestaurantName(), generateHumanDate(reservation.getDate()));

            reservation.getConfirmedUsers().forEach(u -> this.userService.incrementUserVisits(u.getUserId()));
        }

        return reservations;
    }

    public void rejectReservation(String reservationId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESERVATION_ID).is(reservationId));

        Update update = new Update();
        update.pull(TO_CONFIRM_USERS, userId);

        this.mongoTemplate.updateMulti(query, update, RESERVATION_COLLECTION);
    }

    public void confirmReservation(String reservationId, String email) {
        User user = this.userService.findById(email);
        Reservation reservation = this.findById(reservationId);
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());

        if (reservation.getState().equals(ReservationState.CANCELED)) {
            throw new ReservationCanceledException();
        }
        if (user.getVisits() == null || (user.getVisits() != null && user.getVisits() == 0)) {
            throw new NoMoreVisitsException();
        }

        if (reservation.getConfirmedUsers() != null && reservation.getConfirmedUsers().stream().anyMatch(u -> u.equalsIgnoreCase(email))) {
            throw new ReservationAlreadyConfirmedException();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where(RESERVATION_ID).is(reservationId));

        Update update = new Update();
        update.pull(TO_CONFIRM_USERS, email);
        update.push(CONFIRMED_USERS, email);

        this.mongoTemplate.updateMulti(query, update, RESERVATION_COLLECTION);

        this.sendConfirmBookingEmail(reservation, email);
        this.firebaseService.sendConfirmedInvitationNotification(reservation.getOwnerUser(), reservationId,
                user.getName() + " " + user.getLastName(), restaurant.getName(),
                this.generateHumanDate(reservation.getDate()));
        this.userService.decrementUserVisits(email);
    }

    public DoneReservationResponse doneReservation(String reservationId, String restaurantId, String userId, String restaurantUserId) {

        Reservation reservation = this.findById(reservationId);

        if (reservation == null) {
            throw new ReservationNofFoundException();
        }

        UserRestaurant userRestaurant = this.userRestaurantService.findById(restaurantUserId);

        if(userRestaurant == null || !userRestaurant.getRestaurantId().equals(reservation.getRestaurantId())) {
            throw new ReservationNotInThisRestaurantException();
        }

        Restaurant restaurant = this.restaurantService.findById(restaurantId);
        User user = this.userService.findById(userId);

        List<String> userOwnerAndConfirmed = new LinkedList<>(Collections.singletonList(reservation.getOwnerUser()));

        if (reservation.getConfirmedUsers() != null) {
            userOwnerAndConfirmed.addAll(reservation.getConfirmedUsers());
        }

        List<Integer> membershipIds = userOwnerAndConfirmed.stream().map(id -> {
            User oneUser = this.userService.findById(id);
            return oneUser.getActualMembership();
        }).collect(Collectors.toList());

        Map<String, List<Dish>> dishesMap = restaurant.getDishes().stream()
                .filter(dish -> membershipIds.stream().max(Comparator.naturalOrder()).get() >= dish.getBaseMembership())
                .collect(Collectors.groupingBy(Dish::getBaseMembershipName,
                        Collectors.toList()));

        List<Membership> allMemberships = this.membershipRepository.findAll();

        Map<String, Long> membershipMap = allMemberships.stream()
                .collect(Collectors.toMap(Membership::getName,
                        membership -> membershipIds.stream().filter(integer -> membership.getMembershipId().equals(integer)).count()));

        //FIXME commented for test
        //this.updateReservationState(reservationId, ReservationState.DONE);
        //this.firebaseService.sendScoreNotification(userOwnerAndConfirmed, reservation.getRestaurantId(), restaurant.getName());

        DoneReservationResponse response = new DoneReservationResponse();
        response.setReservationId(reservationId);
        response.setOwnerUserName(user.getName() + " " + user.getLastName());
        response.setDinners(reservation.getDinners());
        response.setDate(reservation.getDate().toString());
        response.setDishesPerMembership(dishesMap);
        response.setDinnersPerMembership(membershipMap);

        return response;
    }

    private void updateReservationState(String reservationId, ReservationState state) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESERVATION_ID).is(reservationId));

        Update update = new Update();
        update.set(RESERVATION_STATE, state);

        this.mongoTemplate.updateMulti(query, update, RESERVATION_COLLECTION);
    }

    private String generateHumanDate(LocalDateTime dt) {
        String dayName = dt.getDayOfWeek().getDisplayName(TextStyle.FULL,
                new Locale("es"));

        String monthName = dt.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));

        String hour;
        if (dt.getMinute() == 0) {
            hour = dt.getHour() + ":00";
        } else {
            hour = dt.getHour() + ":" + dt.getMinute();
        }

        return Strings.capitalize(dayName) + " " + dt.getDayOfMonth() + " de " + Strings.capitalize(monthName) + " de " + dt.getYear() + " a las " + hour + "hs";
    }

    private String buildJoinUrl(String reservationId, String userId) {
        return "https://restopass.herokuapp.com/reservations/confirm/" + reservationId + "/" + userId;
    }

    private ReservationResponse mapReservationToResponse(Reservation reservation, String userId) {
        ReservationResponse response = new ReservationResponse();

        response.setReservationId(reservation.getReservationId());
        response.setRestaurantId(reservation.getRestaurantId());
        response.setDate(reservation.getDate());
        response.setQrBase64(reservation.getQrBase64());
        response.setState(reservation.getState());
        this.restaurantService.fillRestaurantData(response);
        if (reservation.getConfirmedUsers() != null && !reservation.getConfirmedUsers().isEmpty()) {
            response.setConfirmedUsers(reservation.getConfirmedUsers().stream().map(this::mapEmailToUserReservation).collect(Collectors.toList()));
        }
        if (reservation.getToConfirmUsers() != null && !reservation.getToConfirmUsers().isEmpty()) {
            response.setToConfirmUsers(reservation.getToConfirmUsers().stream().map(this::mapEmailToUserReservation).collect(Collectors.toList()));
        }
        response.setOwnerUser(mapEmailToUserReservation(reservation.getOwnerUser()));
        if (!reservation.getOwnerUser().equalsIgnoreCase(userId)) response.setInvitation(true);
        response.setDinners(reservation.getDinners());

        return response;
    }

    private UserReservation mapEmailToUserReservation(String userId) {
        User user = this.userService.findById(userId);
        UserReservation response = new UserReservation();

        response.setUserId(userId);
        response.setName(user.getName());
        response.setLastName(user.getLastName());

        return response;
    }

    public Reservation findById(String reservationId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESERVATION_ID).is(reservationId));

        return this.mongoTemplate.findOne(query, Reservation.class);
    }

    public void deleteUserReservations(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(CONFIRMED_USERS).in(userId));
        Update update = new Update();
        update.pull(CONFIRMED_USERS, userId);

        this.mongoTemplate.updateMulti(query, update, RESERVATION_COLLECTION);

        query = new Query();
        query.addCriteria(Criteria.where(TO_CONFIRM_USERS).in(userId));
        update = new Update();
        update.pull(TO_CONFIRM_USERS, userId);

        this.mongoTemplate.updateMulti(query, update, RESERVATION_COLLECTION);

        query = new Query();
        query.addCriteria(Criteria.where(OWNER_USER_ID).is(userId));

        this.mongoTemplate.remove(query, RESERVATION_COLLECTION);
    }

}
