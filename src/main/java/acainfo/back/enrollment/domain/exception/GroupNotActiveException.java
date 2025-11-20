package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to enroll in a group that is not active.
 */
public class GroupNotActiveException extends DomainException {

    public GroupNotActiveException(Long groupId) {
        super("Group with id " + groupId + " is not active and cannot accept enrollments");
    }

    public GroupNotActiveException(String message) {
        super(message);
    }

    public GroupNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
