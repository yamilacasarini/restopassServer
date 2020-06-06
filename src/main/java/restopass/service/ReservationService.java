package restopass.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import restopass.dto.*;
import restopass.mongo.ReservationRepository;
import restopass.utils.EmailSender;
import restopass.utils.QRHelper;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Service
public class ReservationService {

    private MongoTemplate mongoTemplate;
    private ReservationRepository reservationRepository;
    private UserService userService;
    private String RESTAURANT_ID = "restaurantId";
    private String OWNER_USER_ID = "ownerUser";
    private String RESERVATION_ID = "reservationId";
    private String RESERVATION_STATE = "state";
    private String CONFIRMED_USERS = "confirmedUsers";
    private String TO_CONFIRM_USERS = "toConfirmUsers";
    private String SLOTS_FIELD = "slots";
    private String RESERVATION_COLLECTION = "reservations";
    private String RESTAURANT_CONFIG_COLLECTION = "restaurant_configs";
    private Integer SIZE_CALENDAR = 45;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    public ReservationService(MongoTemplate mongoTemplate, ReservationRepository reservationRepository, UserService userService) {
        this.mongoTemplate = mongoTemplate;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
    }

    public void createReservation(Reservation reservation, String userId) {
        String reservationId = UUID.randomUUID().toString();
        reservation.setReservationId(reservationId);

        RestaurantConfig restaurantConfig = this.findConfigurationByRestaurantId(reservation.getRestaurantId());
        List<RestaurantSlot> slots = this.restaurantService.decrementTableInSlot(restaurantConfig, reservation.getDate());
        this.restaurantService.fillRestaurantData(reservation);
        this.updateSlotsInDB(reservation.getRestaurantId(), slots);

        this.userService.decrementUserVisits(userId);

        reservation.setQrBase64(QRHelper.createQRBase64(reservationId, reservation.getRestaurantId(), userId));

        this.sendConfirmBookingEmail(reservation);
        this.sendNewBookingEmail(reservation);

        this.reservationRepository.save(reservation);
    }

    private void sendConfirmBookingEmail(Reservation reservation) {
        User user = this.userService.findById(reservation.getOwnerUser());
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());

        HashMap<String, Object> modelEmail = new HashMap<>();
        modelEmail.put("userName", user.getName());
        modelEmail.put("restaurantName", restaurant.getName());
        modelEmail.put("totalDiners", reservation.getToConfirmUsers().size() + 1);
        modelEmail.put("date", this.generateHumanDate(reservation.getDate()));
        modelEmail.put("restaurantAddress", restaurant.getAddress());
        modelEmail.put("qrCode", reservation.getQrBase64());
        modelEmail.put("cancelDate", reservation.getDate().minusHours(restaurant.getHoursToCancel()));


        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo(reservation.getOwnerUser());
        emailModel.setMailTempate("confirm_booking.html");
        emailModel.setSubject("Tu reserva ha sido confirmada");
        emailModel.setModel(modelEmail);

        EmailSender.sendEmail(emailModel);
    }

    private void sendNewBookingEmail(Reservation reservation) {
        User ownerUser = this.userService.findById(reservation.getOwnerUser());
        Restaurant restaurant = this.restaurantService.findById(reservation.getRestaurantId());

        HashMap<String, Object> modelEmail = new HashMap<>();
        modelEmail.put("ownerUser", ownerUser.getName() + " " + ownerUser.getLastName());
        modelEmail.put("restaurantName", restaurant.getName());
        modelEmail.put("totalDiners", reservation.getToConfirmUsers().size() + 1);
        modelEmail.put("date", this.generateHumanDate(reservation.getDate()));
        modelEmail.put("restaurantAddress", restaurant.getAddress());

        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo(reservation.getOwnerUser());
        emailModel.setMailTempate("new_booking.html");
        emailModel.setSubject("Parece que tienes una nueva reserva");
        emailModel.setModel(modelEmail);

        String toConfirmAddresses = String.join(",", reservation.getToConfirmUsers());

        EmailSender.sendMultipleEmails(emailModel, toConfirmAddresses);
    }

    public List<Reservation> getReservationsForUser(String userId) {
        Query query = new Query();


        Criteria orCriteria = new Criteria();
        orCriteria.orOperator(
                Criteria.where(OWNER_USER_ID).is(userId),
                Criteria.where(CONFIRMED_USERS).in(userId));

        query.addCriteria(orCriteria);

        return this.mongoTemplate.find(query, Reservation.class);
    }

    public Reservation getReservationByAnyUser(String reservationId, String userId) {
        Query query = new Query();

        Criteria orCriteria = new Criteria();
        orCriteria.orOperator(
                Criteria.where(OWNER_USER_ID).is(userId),
                Criteria.where(CONFIRMED_USERS).in(userId),
                Criteria.where(TO_CONFIRM_USERS).in(userId));


        query.addCriteria(Criteria.where(RESERVATION_ID).is(reservationId));
        query.addCriteria(orCriteria);

        return this.mongoTemplate.findOne(query, Reservation.class);
    }

    public void cancelReservation(String reservationId) {
        this.updateReservationState(reservationId, ReservationState.CANCELED);
    }

    public void confirmReservation(String reservationId, String userId) {
        this.userService.decrementUserVisits(userId);

    }

    public void doneReservation(String reservationId, String restaurantId, String userId) {
        //TODO show web with plates
        this.updateReservationState(reservationId, ReservationState.DONE);
    }

    private void updateReservationState(String reservationId, ReservationState state) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESERVATION_ID).is(reservationId));

        Update update = new Update();
        update.set(RESERVATION_STATE, state);

        this.mongoTemplate.updateMulti(query, update, RESERVATION_COLLECTION);
    }

    public RestaurantConfig generateSlotsByRestaurantConfig(String restaurantId) {
        RestaurantConfig restaurantConfig = this.findConfigurationByRestaurantId(restaurantId);
        List<RestaurantHours> restaurantHours = restaurantConfig.getDateTimeAvailable();
        LocalDateTime today = LocalDateTime.now();

        List<RestaurantSlot> slots = new ArrayList<>();

        //Genero el calendario de 45 dias
        IntStream.rangeClosed(0, SIZE_CALENDAR).forEach(i -> {
                    LocalDateTime date = today.plusDays(i);

                    //Busco la configuracion de horas que incluye el dia que estoy queriendo generar.
                    //Si esta cerrado el local, no va a existir esa configuracion por eso Optional
                    Optional<RestaurantHours> optionalDay = restaurantHours.stream().filter(rh -> rh.getOpeningDays().stream().anyMatch(op -> op.equals(date.getDayOfWeek()))).findFirst();

                    if (optionalDay.isPresent()) {
                        RestaurantHours day = optionalDay.get();
                        RestaurantSlot slot = new RestaurantSlot();
                        List<List<DateTimeWithTables>> allDateTimeWithTables = new ArrayList<>();

                        List<PairHour> pairHours = day.getPairHours();

                        //Por cada par hora inicio hora fin genero los distintos horarios con sus mesas
                        pairHours.forEach(pair -> {
                            LocalDateTime startHour = date.withHour(pair.getOpeningHour()).withMinute(pair.getOpeningMinute()).truncatedTo(ChronoUnit.MINUTES);;
                            LocalDateTime endHour = date.withHour(pair.getClosingHour()).withMinute(pair.getClosingMinute()).truncatedTo(ChronoUnit.MINUTES);;
                            List<DateTimeWithTables> dateTimeWithTables = new ArrayList<>();

                            Integer minutes = restaurantConfig.getMinutesGap();
                            int minutesCount = 0;
                            long minutesUntilEndHour = startHour.until(endHour, ChronoUnit.MINUTES);

                            //Empiezo a generar las distintas horas con sus mesas
                            while (minutesCount < minutesUntilEndHour) {
                                DateTimeWithTables dt = new DateTimeWithTables();
                                dt.setDateTime(startHour.plusMinutes(minutesCount));
                                dt.setTablesAvailable(restaurantConfig.getTablesPerShift());
                                dateTimeWithTables.add(dt);
                                minutesCount = minutesCount + minutes;
                            }
                            //Cuando termino de generar todos los horarios para un par inicio fin lo agrego al conjunto de todos los pares inicio fin
                            allDateTimeWithTables.add(dateTimeWithTables);
                        });
                        //Cuando tengo todos los pares inicio fin los agrego al dia
                        slot.setDateTime(allDateTimeWithTables);
                        slots.add(slot);
                    }
                }
        );

        this.updateSlotsInDB(restaurantId, slots);
        restaurantConfig.setSlots(slots);
        return restaurantConfig;
    }

    public RestaurantConfig findConfigurationByRestaurantId(String restaurantId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESTAURANT_ID).is(restaurantId));

        return this.mongoTemplate.findOne(query, RestaurantConfig.class);
    }

    private void updateSlotsInDB(String restaurantId, List<RestaurantSlot> slots) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESTAURANT_ID).is(restaurantId));

        Update update = new Update();
        update.set(SLOTS_FIELD, slots);

        this.mongoTemplate.updateMulti(query, update, RESTAURANT_CONFIG_COLLECTION);
    }

    private String generateHumanDate(LocalDateTime dt) {
        String dayName = dt.getDayOfWeek().getDisplayName(TextStyle.FULL,
                new Locale("es"));

        String monthName = dt.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));

        String hour;
        if(dt.getMinute() == 0) {
            hour = dt.getHour() + ":00";
        } else {
           hour = dt.getHour() + ":" + dt.getMinute();
        }

        return dayName + " de " + monthName + "de " + dt.getYear() + " a las " + hour + "hs";
    }

}
