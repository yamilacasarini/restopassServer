package restopass.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import restopass.dto.*;
import restopass.dto.request.UserCreationRequest;
import restopass.dto.request.UserLoginGoogleRequest;
import restopass.dto.request.UserLoginRequest;
import restopass.dto.request.UserUpdateRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.exception.*;
import restopass.mongo.UserRepository;
import restopass.utils.GoogleLoginUtils;
import restopass.utils.JWTHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private static String EMAIL_FIELD = "email";
    private static String SECONDARY_EMAILS_FIELD = "secondaryEmails";
    private static String PASSWORD_FIELD = "password";
    private static String NAME_FIELD = "name";
    private static String LAST_NAME_FIELD = "lastName";
    private static String VISITS_FIELD = "visits";
    private static String B2B_FIELD = "b2BUserEmployee";
    private static String MEMBERSHIP_FINALIZE_DATE_FIELD = "membershipFinalizeDate";
    private static String ACTUAL_MEMBERSHIP = "actualMembership";
    private static String FAVORITE_RESTAURANTS_FIELD = "favoriteRestaurants";
    private static String CREDIT_CARD_FIELD = "creditCard";
    private static String USER_COLLECTION = "users";
    private static String ACCESS_TOKEN_HEADER = "X-Auth-Token";
    private static String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    MongoTemplate mongoTemplate;
    UserRepository userRepository;
    GoogleLoginUtils googleLoginUtils;

    @Autowired
    B2BUserService b2bUserService;

    @Autowired
    public UserService(MongoTemplate mongoTemplate, UserRepository userRepository, GoogleLoginUtils googleLoginUtils) {
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.googleLoginUtils = googleLoginUtils;
    }

    public UserLoginResponse loginUser(UserLoginRequest user) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(user.getEmail()));
        query.addCriteria(Criteria.where(PASSWORD_FIELD).is(user.getPassword()));

        User userDTO = this.mongoTemplate.findOne(query, User.class);

        if (userDTO == null) {
            throw new InvalidUsernameOrPasswordException();
        }

        return this.buildUserLoginResponse(userDTO, false);
    }

    public UserLoginResponse loginGoogleUser(UserLoginGoogleRequest userRequest) {
        User newUser = googleLoginUtils.verifyGoogleToken(userRequest.getGoogleToken());
        User userDB = this.findById(newUser.getEmail());

        if (userDB == null) {
            userRepository.save(newUser);
            return this.buildUserLoginResponse(newUser, true);
        } else {
            return this.buildUserLoginResponse(userDB, false);
        }
    }

    public UserLoginResponse createUser(UserCreationRequest user) {
        User userDTO = new User(user.getEmail(), user.getPassword(), user.getName(), user.getLastName());
        B2BUserEmployer b2BUserEmployer = this.b2bUserService.checkIfB2BUser(user.getEmail());

        if (b2BUserEmployer != null) {
            userDTO.setB2BUserEmployee(new B2BUserEmployee(b2BUserEmployer.getPercentageDiscountPerMembership(), b2BUserEmployer.getCompanyName()));
        }

        try {
            userRepository.save(userDTO);
            return this.buildUserLoginResponse(userDTO, true);
        } catch (DuplicateKeyException e) {
            throw new UserAlreadyExistsException();
        }
    }

    public UserLoginResponse refreshToken(HttpServletRequest req) {

        String oldAccessToken = req.getHeader(ACCESS_TOKEN_HEADER);
        String refreshAccessToken = req.getHeader(REFRESH_TOKEN_HEADER);

        String emailRefresh = JWTHelper.decodeJWT(refreshAccessToken).getId();
        User userDTO = this.findById(emailRefresh);

        try {
            Claims claims = JWTHelper.decodeJWT(oldAccessToken);
            if (claims.getId().equalsIgnoreCase(emailRefresh)) {
                return this.buildUserLoginResponse(userDTO, false);
            } else {
                throw new InvalidAccessOrRefreshTokenException();
            }
        } catch (ExpiredJwtException e) {
            if (e.getClaims().getId().equalsIgnoreCase(emailRefresh)) {
                return this.buildUserLoginResponse(userDTO, false);
            } else {
                throw new InvalidAccessOrRefreshTokenException();
            }
        } catch (Exception e) {
            throw new InvalidAccessOrRefreshTokenException();
        }
    }

    public User findById(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        return this.mongoTemplate.findOne(query, User.class);
    }

    public void decrementUserVisits(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update().inc(VISITS_FIELD, -1);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public void incrementUserVisits(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update().inc(VISITS_FIELD, 1);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public void updateMembership(String userId, Membership membership) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        update.set(ACTUAL_MEMBERSHIP, membership.getMembershipId());
        update.set(VISITS_FIELD, membership.getVisits());
        update.unset(MEMBERSHIP_FINALIZE_DATE_FIELD);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public void removeMembership(String userId) {
        LocalDateTime membershipFinalizeDate = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusDays(1);
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        update.unset(ACTUAL_MEMBERSHIP);
        update.set(MEMBERSHIP_FINALIZE_DATE_FIELD, membershipFinalizeDate);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    private UserLoginResponse buildUserLoginResponse(User user, Boolean isCreation) {
        UserLoginResponse userResponse = new UserLoginResponse();
        userResponse.setxAuthToken(JWTHelper.createAccessToken(user.getEmail()));
        userResponse.setxRefreshToken(JWTHelper.createRefreshToken(user.getEmail()));
        userResponse.setUser(user);
        userResponse.setCreation(isCreation);
        return userResponse;
    }

    public void addNewRestaurantFavorite(String restaurantId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));
        Update update = new Update();
        update.push(FAVORITE_RESTAURANTS_FIELD, restaurantId);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public void removeRestaurantFavorite(String restaurantId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));
        Update update = new Update();
        update.pull(FAVORITE_RESTAURANTS_FIELD, restaurantId);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public User checkCanAddToReservation(String userId, Integer baseMembership) {
        Query query = new Query();

        Criteria orCriteria = new Criteria();
        orCriteria.orOperator(
                Criteria.where(EMAIL_FIELD).is(userId.trim()),
                Criteria.where(SECONDARY_EMAILS_FIELD).in(userId));

        query.addCriteria(orCriteria);

        User user = this.mongoTemplate.findOne(query, User.class);


        if (user == null) {
            throw new UserNotFoundException();
        }

        if (user.getActualMembership() >= baseMembership) {
            return user;
        } else {
            throw new RestaurantNotInMembershipException();
        }
    }

    public void updateUserInfo(UserUpdateRequest request, String userId) {

        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        this.setIfNotEmpty(NAME_FIELD, request.getName(), update);
        this.setIfNotEmpty(LAST_NAME_FIELD, request.getLastName(), update);
        this.setIfNotEmpty(PASSWORD_FIELD, request.getPassword(), update);
        this.pushSecondaryEmailIfNotEmpty(request.getSecondaryEmail(), userId, update);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public CreditCard getPayment(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        User user = this.mongoTemplate.findOne(query, User.class);

        if (user == null) {
            throw new UserNotFoundException();
        }

        if (user.getCreditCard() == null) {
            throw new NoCreditCardException();
        }

        return user.getCreditCard();
    }

    public void updatePayment(CreditCard creditCard, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update().set(CREDIT_CARD_FIELD, creditCard);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);

    }

    public void removePayment(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update().unset(CREDIT_CARD_FIELD);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    private void pushSecondaryEmailIfNotEmpty(String email, String userId, Update update) {
        if (email != null) {
            if (userId.equalsIgnoreCase(email)) {
                throw new EmalAlreadyExistsException();
            }
            update.push(SECONDARY_EMAILS_FIELD, email);
        }
    }

    private void setIfNotEmpty(String propertyName, String value, Update update) {
        if (value != null) {
            update.set(propertyName, value);
        }
    }

    public void setB2BUserToEmployees(String employee, List<Float> percentageDiscountPerMembership, String companyName) {
        User user = this.findById(employee);

        if(user != null) {
            Query query = new Query();
            query.addCriteria(Criteria.where(EMAIL_FIELD).is(employee));
            Update update = new Update().set(B2B_FIELD, new B2BUserEmployee(percentageDiscountPerMembership, companyName));

            this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
        }
    }
}
