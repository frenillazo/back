package acainfo.back.user.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when attempting to assign a non-teacher user to a subjectGroup.
 */
public class InvalidTeacherException extends DomainException {

    public InvalidTeacherException(Long userId) {
        super("User with id " + userId + " is not a teacher and cannot be assigned to a subjectGroup");
    }

    public InvalidTeacherException(String email) {
        super("User " + email + " is not a teacher and cannot be assigned to a subjectGroup");
    }

    public InvalidTeacherException(String message, Throwable cause) {
        super(message, cause);
    }
}
