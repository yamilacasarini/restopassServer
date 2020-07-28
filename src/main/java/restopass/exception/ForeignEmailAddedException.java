package restopass.exception;

public class ForeignEmailAddedException extends RestoPassException {

    public ForeignEmailAddedException() {
        super(ErrorCode.FOREIGN_EMAIL_ADDED);
    }
}