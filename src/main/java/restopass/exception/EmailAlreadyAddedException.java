package restopass.exception;

public class EmailAlreadyAddedException  extends RestoPassException {

    public EmailAlreadyAddedException() {
        super(ErrorCode.EMAIL_ALREADY_ADDED);
    }
}