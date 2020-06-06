package restopass.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.BAD_REQUEST.value(), 1, "Tu usuario o contraseña son incorrectos"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST.value(), 2, "El email ya se encuentra registrado en nuestro sistema"),
    ACCESS_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST.value(), 3, "Access token is required for this request"),
    ACCESS_REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST.value(), 4, "Invalid access or refresh token"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 1, "El usuario no existe"),

    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), 1, "Expired access token"),
    EMPTY_VISITS(HttpStatus.UNAUTHORIZED.value(), 2, "¿Quieres seguir visitando tus restaurantes favoritos? Porque no pruebas un mejor plan"),
    RESERVATION_ALREADY_CONFIRMED(HttpStatus.UNAUTHORIZED.value(), 3, "Reserva ya confirmada"),
    RESERVATION_CANCELED(HttpStatus.UNAUTHORIZED.value(), 4, "Reserva cancelada por el dueño");

    private Integer status;
    private Integer code;
    private String message;

    ErrorCode(Integer status, Integer code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Integer getValue() {
        if (this.code != null) {
            return this.status * 100 + this.code;
        }

        return this.status;
    }
}
