package restopass.exception;

import restopass.dto.Restaurant;

public class NoMoreVisitsException extends RestoPassException {
    public NoMoreVisitsException() {
        super(ErrorCode.EMPTY_VISITS);
    }
}
