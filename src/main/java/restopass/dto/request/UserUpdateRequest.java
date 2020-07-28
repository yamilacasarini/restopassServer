package restopass.dto.request;

public class UserUpdateRequest {

    private String name;
    private String lastName;
    private String password;
    private String toConfirmEmail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToConfirmEmail() {
        return toConfirmEmail;
    }

    public void setToConfirmEmail(String toConfirmEmail) {
        this.toConfirmEmail = toConfirmEmail;
    }
}
