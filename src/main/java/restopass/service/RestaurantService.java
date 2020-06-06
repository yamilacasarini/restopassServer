package restopass.service;

import io.jsonwebtoken.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import restopass.dto.*;
import restopass.dto.request.RestaurantCreationRequest;
import restopass.dto.response.RestaurantTagsResponse;
import restopass.mongo.FiltersMapRepository;
import restopass.mongo.RestaurantConfigRepository;
import restopass.mongo.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RestaurantService {

    protected final MongoTemplate mongoTemplate;
    final RestaurantRepository restaurantRepository;
    final FiltersMapRepository filtersMapRepository;
    final RestaurantConfigRepository restaurantConfigRepository;
    @Autowired
    private MembershipService membershipService;

    private String RESTAURANT_ID = "restaurantId";
    private String DISHES_FIELD = "dishes";
    private String TOP_MEMBERSHIP_FIELD = "topMembership";
    private String LOCATION_FIELD = "location";
    private String RESTAURANTS_COLLECTION = "restaurants";
    private String TAGS_FIELD = "tags";
    private Double KM_RADIUS = 10D;


    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository,
                             FiltersMapRepository filtersMapRepository,
                             MongoTemplate mongoTemplate, RestaurantConfigRepository restaurantConfigRepository) {
        this.restaurantRepository = restaurantRepository;
        this.filtersMapRepository = filtersMapRepository;
        this.restaurantConfigRepository = restaurantConfigRepository;
        this.mongoTemplate = mongoTemplate;

    }

    public void createRestaurant(RestaurantCreationRequest restaurantCreation) {
        Restaurant restaurant = new Restaurant();
        String restaurantId = UUID.randomUUID().toString();
        restaurant.setRestaurantId(restaurantId);

        restaurant.setAddress(restaurantCreation.getAddress());
        restaurant.setImg(restaurantCreation.getImg());
        GeoJsonPoint point = new GeoJsonPoint(restaurantCreation.getLongitude(), restaurantCreation.getLatitude());
        restaurant.setLocation(point);
        restaurant.setName(restaurantCreation.getName());
        restaurant.setTimeTable(restaurantCreation.getTimeTable());
        restaurant.setTags(restaurantCreation.getTags());
        restaurant.setDishes(restaurantCreation.getDishes());

        this.restaurantRepository.save(restaurant);
    }

    public void createRestaurantConfig(RestaurantConfig restaurantConfig) {
        this.restaurantConfigRepository.save(restaurantConfig);
    }

    public List<RestaurantSlot> decrementTableInSlot(RestaurantConfig restaurantConfig, LocalDateTime dateTime) {
        restaurantConfig.getSlots().forEach(
                slot ->
                        slot.getDateTime().forEach(dt ->
                                dt.forEach(date -> {
                                            if (date.getDateTime().equals(dateTime)) {
                                                date.setTablesAvailable(date.getTablesAvailable() - 1);
                                            }
                                        }
                                )
                        )
        );

        return restaurantConfig.getSlots();
    }

    public void addDish(Dish dish, String restaurantId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESTAURANT_ID).is(restaurantId));

        Update update = new Update();
        update.addToSet(DISHES_FIELD, dish);
        this.mongoTemplate.updateMulti(query, update, RESTAURANTS_COLLECTION);
    }

    public List<Restaurant> getInARadius(Double lat, Double lng) {
        Query query = new Query();

        Point geoPoint = new Point(lng, lat);
        Distance geoDistance = new Distance(KM_RADIUS, Metrics.KILOMETERS);
        Circle geoCircle = new Circle(geoPoint, geoDistance);
        query.addCriteria(Criteria.where(LOCATION_FIELD).withinSphere(geoCircle));

        return this.mongoTemplate.find(query, Restaurant.class);
    }

    public List<Restaurant> getByTags(List<String> tags, MembershipType topMembership, String freeText) {
        tags.addAll(Arrays.asList(Strings.delimitedListToStringArray(freeText, " ")));
        Query query = new Query();
        query.addCriteria(Criteria.where(TAGS_FIELD).all(tags));
        query.addCriteria(Criteria.where(DISHES_FIELD).elemMatch(Criteria.where(TOP_MEMBERSHIP_FIELD).lte(topMembership.ordinal())));

        return this.mongoTemplate.find(query, Restaurant.class);
    }

    public List<Restaurant> getRestaurantInAMemberships(MembershipType membership) {
        Query query = new Query();
        query.addCriteria(Criteria.where(DISHES_FIELD).elemMatch(Criteria.where(TOP_MEMBERSHIP_FIELD).lte(membership.ordinal())));

        return this.mongoTemplate.find(query, Restaurant.class);
    }

    public RestaurantTagsResponse getRestaurantsTags() {
        RestaurantTagsResponse response = new RestaurantTagsResponse();

        List<FilterMap> filtersMap = this.filtersMapRepository.findAll();
        List<Membership> memberships = membershipService.findAll();

        HashMap<String, List<String>> tags = new HashMap<>();
        List<MembershipType> membershipTypes = new ArrayList<>();

        memberships.forEach(membership -> membershipTypes.add(membership.getMembershipId()));
        filtersMap.forEach(filter -> tags.put(filter.getName(), filter.getElements()));

        response.setMemberships(membershipTypes);
        response.setTags(tags);

        return response;
    }

    public Restaurant findById(String restaurantId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(RESTAURANT_ID).is(restaurantId));

        return this.mongoTemplate.findOne(query, Restaurant.class);
    }


    public void fillRestaurantData(Reservation reservation) {
        Restaurant restaurant = this.findById(reservation.getRestaurantId());
        reservation.setRestaurantAddress(restaurant.getAddress());
        reservation.setRestaurantName(restaurant.getName());
    }
}
