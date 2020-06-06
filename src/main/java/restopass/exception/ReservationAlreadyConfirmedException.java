package restopass.exception;

public class ReservationAlreadyConfirmedException extends RestoPassException {
    public ReservationAlreadyConfirmedException() {
        super(ErrorCode.RESERVATION_ALREADY_CONFIRMED);
    }
}
