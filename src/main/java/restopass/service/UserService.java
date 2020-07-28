package restopass.service;

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
import restopass.dto.request.UserUpdateRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.exception.*;
import restopass.mongo.UserRepository;
import restopass.utils.EmailSender;
import restopass.utils.JWTHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService extends GenericUserService {

    private static String EMAIL_FIELD = "email";
    private static String PASSWORD_FIELD = "password";
    private static String NAME_FIELD = "name";
    private static String LAST_NAME_FIELD = "lastName";
    private static String VISITS_FIELD = "visits";
    private static String B2B_FIELD = "b2BUserEmployee";
    private static String MEMBERSHIP_FINALIZE_DATE_FIELD = "membershipFinalizeDate";
    private static String MEMBERSHIP_ENROLLED_DATE_FIELD = "membershipEnrolledDate";
    private static String ACTUAL_MEMBERSHIP = "actualMembership";
    private static String FAVORITE_RESTAURANTS_FIELD = "favoriteRestaurants";
    private static String CREDIT_CARD_FIELD = "creditCard";
    private static String SECONDARY_EMAILS_FIELD = "secondaryEmails";
    private static String TO_CONFIRM_EMAILS_FIELD = "toConfirmEmails";
    private static String USER_COLLECTION = "users";
    private static String ACCESS_TOKEN_HEADER = "X-Auth-Token";
    private static String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    MongoTemplate mongoTemplate;
    UserRepository userRepository;
    GoogleService googleService;

    @Autowired
    B2BUserService b2bUserService;

    @Autowired
    public UserService(MongoTemplate mongoTemplate, UserRepository userRepository, GoogleService googleService) {
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.googleService = googleService;
    }

    public User findByUserAndPass(Query query) {
        return this.mongoTemplate.findOne(query, User.class);
    }

    public UserLoginResponse<User> loginGoogleUser(UserLoginGoogleRequest userRequest) {
        User newUser = googleService.verifyGoogleToken(userRequest.getGoogleToken());
        User userDB = this.findById(newUser.getEmail());

        if (userDB == null) {
            userRepository.save(newUser);
            return JWTHelper.buildUserLoginResponse(newUser, newUser.getEmail(),true);
        } else {
            return JWTHelper.buildUserLoginResponse(userDB, userDB.getEmail(),false);
        }
    }

    public  UserLoginResponse<User> createUser(UserCreationRequest user) {
        User userDTO = new User(user.getEmail(), user.getPassword(), user.getName(), user.getLastName());
        B2BUserEmployer b2BUserEmployer = this.b2bUserService.checkIfB2BUser(user.getEmail());

        if (b2BUserEmployer != null) {
            userDTO.setB2BUserEmployee(new B2BUserEmployee(b2BUserEmployer.getPercentageDiscountPerMembership(), b2BUserEmployer.getCompanyName()));
        }

        try {
            userRepository.save(userDTO);
            return JWTHelper.buildUserLoginResponse(userDTO, userDTO.getEmail(),true);
        } catch (DuplicateKeyException e) {
            throw new UserAlreadyExistsException();
        }
    }

    public User findById(String userId) {
        Criteria orCriteria = new Criteria();
        orCriteria.orOperator(
                Criteria.where(EMAIL_FIELD).is(userId),
                Criteria.where(SECONDARY_EMAILS_FIELD).in(userId));

        Query query = new Query();
        query.addCriteria(orCriteria);

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

    public LocalDateTime updateMembership(String userId, Membership membership) {
        LocalDateTime enrolledDate = LocalDateTime.now();
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        update.set(ACTUAL_MEMBERSHIP, membership.getMembershipId());
        update.set(VISITS_FIELD, membership.getVisits());
        update.unset(MEMBERSHIP_FINALIZE_DATE_FIELD);
        update.set(MEMBERSHIP_ENROLLED_DATE_FIELD, enrolledDate);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);

        return enrolledDate;
    }

    public LocalDateTime removeMembership(String userId) {
        User user = this.findById(userId);
        LocalDateTime membershipFinalizeDate = LocalDateTime.now().withDayOfMonth(user.getMembershipEnrolledDate().getDayOfMonth()).plusDays(30);

        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        update.unset(ACTUAL_MEMBERSHIP);
        update.unset(MEMBERSHIP_ENROLLED_DATE_FIELD);
        update.set(MEMBERSHIP_FINALIZE_DATE_FIELD, membershipFinalizeDate);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);

        return membershipFinalizeDate;
    }

    public UserLoginResponse<User> refreshToken(HttpServletRequest req) {
        String refreshAccessToken = req.getHeader(REFRESH_TOKEN_HEADER);
        String oldAccessToken = req.getHeader(ACCESS_TOKEN_HEADER);

        String emailRefresh = JWTHelper.decodeJWT(refreshAccessToken).getId();
        User user = this.findById(emailRefresh);

        return JWTHelper.refreshToken(oldAccessToken, emailRefresh, user);
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
        User user = this.findById(userId);

        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        this.setIfNotEmpty(NAME_FIELD, request.getName(), update);
        this.setIfNotEmpty(LAST_NAME_FIELD, request.getLastName(), update);
        this.setIfNotEmpty(PASSWORD_FIELD, request.getPassword(), update);
        this.pushToConfirmEmailIfNotEmpty(request.getToConfirmEmail(), user, update);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public void removeEmail(String email, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        update.pull(SECONDARY_EMAILS_FIELD, email);
        update.pull(TO_CONFIRM_EMAILS_FIELD, email);

        this.mongoTemplate.updateMulti(query, update, USER_COLLECTION);
    }

    public void confirmEmail(String email, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(userId));

        Update update = new Update();
        update.pull(TO_CONFIRM_EMAILS_FIELD, email);
        update.addToSet(SECONDARY_EMAILS_FIELD, email);

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

    private void pushToConfirmEmailIfNotEmpty(String email, User user, Update update) {
        if (email != null) {
            if (user.getSecondaryEmails().contains(email) || user.getToConfirmEmails().contains(email)) {
                throw new EmailAlreadyAddedException();
            }
            if (user.getEmail().equalsIgnoreCase(email)) {
                throw new EmailAlreadyExistsException();
            }

            User anotherUser = this.findById(email);
            if (anotherUser != null) {
                throw new ForeignEmailAddedException();
            }

            update.addToSet(TO_CONFIRM_EMAILS_FIELD, email);

            sendToConfirmEmail(email, user.getName(), user.getEmail());

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

    private void sendToConfirmEmail(String email, String name, String userId) {
        HashMap<String, Object> modelEmail = new HashMap<>();
        modelEmail.put("name", name);
        modelEmail.put("userId", userId);
        modelEmail.put("email", email);


        EmailModel emailModel = new EmailModel();
        emailModel.setEmailTo(email);
        emailModel.setMailTempate("/confirmEmail/confirm-email.ftl");
        emailModel.setSubject("Confirma tu email");
        emailModel.setModel(modelEmail);

        EmailSender.sendEmail(emailModel);
    }
}
