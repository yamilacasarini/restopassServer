package restopass.exception;

public class RestoPassException extends RuntimeException {

    private Integer code;

    public RestoPassException(ErrorCode error) {
        super(error.getMessage());
        this.code = error.getValue();
        this.setStackTrace(new StackTraceElement[0]);
    }

    public RestoPassException(ErrorCode error, String msg) {
        super(error.getMessage().replace("{}", msg));
        this.code = error.getValue();
        this.setStackTrace(new StackTraceElement[0]);
    }

    public Integer getCode() {
        return code;
    }

    public int getHttpStatusCode() {
        return this.code < 1000 ? this.code : Integer.parseInt(String.valueOf(this.code).substring(0, 3));
    }

}
