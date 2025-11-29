package acainfo.back.subjectgroup.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when attempting to enroll in a full subjectGroup.
 */
public class GroupFullException extends DomainException {

    public GroupFullException(Long groupId) {
        super("SubjectGroup with id " + groupId + " is full and cannot accept more enrollments");
    }

    public GroupFullException(Long groupId, int maxCapacity) {
        super("SubjectGroup with id " + groupId + " is full (capacity: " + maxCapacity + ")");
    }

    public GroupFullException(String message) {
        super(message);
    }
}
