package acainfo.back.session.domain.exception;

/**
 * Exception thrown when a session is not found.
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(Long sessionId) {
        super("Session not found with ID: " + sessionId);
    }

    public SessionNotFoundException(String message) {
        super(message);
    }

    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
