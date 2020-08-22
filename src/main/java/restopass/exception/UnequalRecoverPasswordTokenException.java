package restopass.exception;

public class UnequalRecoverPasswordTokenException extends RestoPassException {
    public UnequalRecoverPasswordTokenException() {
        super(ErrorCode.UNEQUAL_RECOVER_PASSWORD_TOKEN);
    }
}
