package restopass.exception;

public class DeleteUserBadPasswordException extends RestoPassException {
    public DeleteUserBadPasswordException() {
        super(ErrorCode.DELETE_USER_BAD_PASSWORD);
    }
}
