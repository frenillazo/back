package acainfo.back.user.domain.exception;

import acainfo.back.config.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}
