package acainfo.back.domain.exception;

/**
 * Exception thrown when attempting to assign a non-teacher user to a group.
 */
public class InvalidTeacherException extends DomainException {

    public InvalidTeacherException(Long userId) {
        super("User with id " + userId + " is not a teacher and cannot be assigned to a group");
    }

    public InvalidTeacherException(String email) {
        super("User " + email + " is not a teacher and cannot be assigned to a group");
    }

    public InvalidTeacherException(String message, Throwable cause) {
        super(message, cause);
    }
}
