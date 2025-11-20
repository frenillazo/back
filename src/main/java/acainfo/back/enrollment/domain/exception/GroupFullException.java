package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to enroll in a full group.
 * This exception is raised when the group has reached maximum capacity
 * and the student doesn't qualify for online mode (needs 2+ active enrollments).
 */
public class GroupFullException extends DomainException {

    public GroupFullException(Long groupId) {
        super("Group with id " + groupId + " is full and student does not qualify for online mode");
    }

    public GroupFullException(String message) {
        super(message);
    }

    public GroupFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
