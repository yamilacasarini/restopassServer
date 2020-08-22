package restopass.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.BAD_REQUEST.value(), 1, "Tu usuario o contraseña son incorrectos"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST.value(), 2, "El email ya se encuentra registrado en nuestro sistema"),
    ACCESS_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST.value(), 3, "Access token is required for this request"),
    ACCESS_REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST.value(), 4, "Invalid access or refresh token"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST.value(), 5, "El email secundario debe ser diferente al principal"),
    INVALID_USER_GOOGLE_LOGIN(HttpStatus.BAD_REQUEST.value(), 6, "No se pudo validar el usuario con Google"),
    BAD_IMG_REQUEST_TO_FIREBASE(HttpStatus.BAD_REQUEST.value(), 7, "No se pudo obtener la imagen para subir a firebase para {}, pruebe con otra"),
    EMPTY_VISITS(HttpStatus.BAD_REQUEST.value(), 8, "No tienes suficientes visitas ¿Quieres seguir visitando tus restaurantes favoritos? Porque no pruebas un mejor plan"),
    RESERVATION_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST.value(), 9, "Reserva ya confirmada"),
    RESERVATION_CANCELED(HttpStatus.BAD_REQUEST.value(), 10, "Reserva cancelada por el dueño"),
    RESERVATION_CANCEL_TIME_EXPIRED(HttpStatus.BAD_REQUEST.value(), 11, "Se excedió el tiempo para cancelar la reserva. Lamentablemente el restaurante no acepta tu cancelación"),
    EMAIL_ALREADY_ADDED(HttpStatus.BAD_REQUEST.value(), 12, "Ya agregaste este email"),
    FOREIGN_EMAIL_ADDED(HttpStatus.BAD_REQUEST.value(), 13, "Parece que otro usuario ya tiene este email"),
    RESERVATION_NOT_OF_THIS_RESTAURANT(HttpStatus.BAD_REQUEST.value(), 14, "Parece que esta reserva no pertenece a este restaurante"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 1, "El usuario no esta registrado en RestoPass"),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 2, "Url doesnt exist. Please check our wiki for more info"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 3, "La reserva no existe"),
    LAST_TABLE_ALREADY_BOOKED(HttpStatus.NOT_FOUND.value(), 4, "Ups, Se reservó la última mesa para este día y horario"),
    CREDIT_CARD_NOT_FOND(HttpStatus.NOT_FOUND.value(), 5, "No tienes tarjetas de crédito"),

    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), 1, "Expired access token"),
    RESTAURANT_NOT_IN_MEMBERSHIP(HttpStatus.UNAUTHORIZED.value(), 2, "El restaurante no tiene platos disponibles en la membresía de este usuario"),
    UNEQUAL_RECOVER_PASSWORD_TOKEN(HttpStatus.UNAUTHORIZED.value(), 3, "El código ingresado es incorrecto. Te hemos enviado un nuevo código a tu correo"),

    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 0, "Ups, server exploded. Contact Yami");

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
