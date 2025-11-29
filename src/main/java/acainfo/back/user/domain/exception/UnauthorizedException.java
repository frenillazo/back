package acainfo.back.user.domain.exception;

import acainfo.back.config.exception.DomainException;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException() {
        super("Unauthorized access");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
