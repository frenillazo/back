package acainfo.back.domain.exception;

/**
 * Exception thrown when attempting to enroll in a full group.
 */
public class GroupFullException extends DomainException {

    public GroupFullException(Long groupId) {
        super("Group with id " + groupId + " is full and cannot accept more enrollments");
    }

    public GroupFullException(Long groupId, int maxCapacity) {
        super("Group with id " + groupId + " is full (capacity: " + maxCapacity + ")");
    }

    public GroupFullException(String message) {
        super(message);
    }
}
