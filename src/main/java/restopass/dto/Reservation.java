package restopass.dto;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "reservations")
public class Reservation {

    private String reservationId;
    private String restaurantId;
    private LocalDateTime date;
    private ReservationState state = ReservationState.CONFIRMED;
    private String ownerUser;
    private String qrBase64;
    private List<String> confirmedUsers;
    private List<String> toConfirmUsers;
    private String restaurantName;
    private String restaurantAddress;

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public String getQrBase64() {
        return qrBase64;
    }

    public void setQrBase64(String qrBase64) {
        this.qrBase64 = qrBase64;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ReservationState getState() {
        return state;
    }

    public void setState(ReservationState reservationState) {
        this.state = reservationState;
    }

    public String getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(String ownerUser) {
        this.ownerUser = ownerUser;
    }

    public List<String> getConfirmedUsers() {
        return confirmedUsers;
    }

    public void setConfirmedUsers(List<String> confirmedUsers) {
        this.confirmedUsers = confirmedUsers;
    }

    public List<String> getToConfirmUsers() {
        return toConfirmUsers;
    }

    public void setToConfirmUsers(List<String> toConfirmUsers) {
        this.toConfirmUsers = toConfirmUsers;
    }
}
