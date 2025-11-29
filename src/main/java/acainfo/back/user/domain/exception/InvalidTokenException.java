package acainfo.back.user.domain.exception;

import acainfo.back.config.exception.DomainException;

public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        super("Invalid or expired token");
    }
}
