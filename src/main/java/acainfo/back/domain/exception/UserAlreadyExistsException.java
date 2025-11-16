package acainfo.back.domain.exception;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}
