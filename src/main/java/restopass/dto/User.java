package restopass.dto;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
public class User extends GenericUser {

    private String name;
    private String lastName;
    private CreditCard creditCard;
    private Integer visits;
    private Integer actualMembership;
    private List<String> secondaryEmails;
    private Set<String> favoriteRestaurants;
    private B2BUserEmployee b2BUserEmployee;
    private LocalDateTime membershipFinalizeDate;
    private LocalDateTime membershipEnrolledDate;

    public User() {
        super();
    }

    public User(String email, String password, String name, String lastName) {
        super(email, password);
        this.name = name;
        this.lastName = lastName;
    }

    public LocalDateTime getMembershipEnrolledDate() {
        return membershipEnrolledDate;
    }

    public void setMembershipEnrolledDate(LocalDateTime membershipEnrolledDate) {
        this.membershipEnrolledDate = membershipEnrolledDate;
    }

    public LocalDateTime getMembershipFinalizeDate() {
        return membershipFinalizeDate;
    }

    public void setMembershipFinalizeDate(LocalDateTime membershipFinalizeDate) {
        this.membershipFinalizeDate = membershipFinalizeDate;
    }

    public Set<String> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    public void setFavoriteRestaurants(Set<String> favoriteRestaurants) {
        this.favoriteRestaurants = favoriteRestaurants;
    }

    public List<String> getSecondaryEmails() {
        return secondaryEmails;
    }

    public void setSecondaryEmails(List<String> secondaryEmails) {
        this.secondaryEmails = secondaryEmails;
    }

    public Integer getVisits() {
        return visits;
    }

    public void setVisits(Integer visits) {
        this.visits = visits;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public Integer getActualMembership() {
        return actualMembership;
    }

    public void setActualMembership(Integer actualMembership) {
        this.actualMembership = actualMembership;
    }

    public B2BUserEmployee getB2BUserEmployee() {
        return b2BUserEmployee;
    }

    public void setB2BUserEmployee(B2BUserEmployee b2BUserEmployee) {
        this.b2BUserEmployee = b2BUserEmployee;
    }

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
}
