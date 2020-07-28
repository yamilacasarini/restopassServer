package restopass.dto;

import org.springframework.data.mongodb.core.index.Indexed;

public class GenericUser {

    @Indexed(unique = true)
    private String email;
    private String password;

    public GenericUser() {
    }

    public GenericUser(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
