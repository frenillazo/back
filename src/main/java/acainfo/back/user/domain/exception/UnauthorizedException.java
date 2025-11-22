package acainfo.back.user.domain.exception;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException() {
        super("Unauthorized access");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
