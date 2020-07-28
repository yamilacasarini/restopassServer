package restopass.exception;

public class EmailAlreadyExistsException extends RestoPassException {

    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}