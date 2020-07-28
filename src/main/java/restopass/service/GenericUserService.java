package restopass.service;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import restopass.exception.InvalidUsernameOrPasswordException;
import restopass.dto.request.UserLoginRequest;
import restopass.dto.response.UserLoginResponse;
import restopass.utils.JWTHelper;


@Service
public abstract class GenericUserService {

    private static String EMAIL_FIELD = "email";
    private static String PASSWORD_FIELD = "password";

    public <T> UserLoginResponse<T> loginUser(UserLoginRequest user) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EMAIL_FIELD).is(user.getEmail()));
        query.addCriteria(Criteria.where(PASSWORD_FIELD).is(user.getPassword()));

        if (this.findByUserAndPass(query) == null) {
            throw new InvalidUsernameOrPasswordException();
        }

        return JWTHelper.buildUserLoginResponse(this.findByUserAndPass(query), user.getEmail(),false);
    }

    public abstract <T> T findByUserAndPass(Query query);
}
