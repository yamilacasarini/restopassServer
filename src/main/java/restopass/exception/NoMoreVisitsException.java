package restopass.exception;

public class NoMoreVisitsException extends RestoPassException {
    public NoMoreVisitsException() {
        super(ErrorCode.EMPTY_VISITS);
    }
}
