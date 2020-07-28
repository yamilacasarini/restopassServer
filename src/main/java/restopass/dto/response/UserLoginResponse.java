package restopass.dto.response;

public class UserLoginResponse<T> {

    private String xAuthToken;
    private String xRefreshToken;
    private T user;
    private boolean isCreation;

    public UserLoginResponse(String xAuthToken, String xRefreshToken, T user) {
        this.xAuthToken = xAuthToken;
        this.xRefreshToken = xRefreshToken;
        this.user = user;
    }

    public boolean isCreation() {
        return isCreation;
    }

    public void setCreation(boolean creation) {
        isCreation = creation;
    }

    public String getxAuthToken() {
        return xAuthToken;
    }

    public void setxAuthToken(String xAuthToken) {
        this.xAuthToken = xAuthToken;
    }

    public String getxRefreshToken() {
        return xRefreshToken;
    }

    public void setxRefreshToken(String xRefreshToken) {
        this.xRefreshToken = xRefreshToken;
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }
}

