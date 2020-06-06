package restopass.exception;

public class ReservationCanceledException extends RestoPassException {
    public ReservationCanceledException() {
        super(ErrorCode.RESERVATION_CANCELED);
    }
}
