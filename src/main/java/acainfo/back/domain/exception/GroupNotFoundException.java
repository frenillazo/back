package acainfo.back.domain.exception;

/**
 * Exception thrown when a group is not found.
 */
public class GroupNotFoundException extends DomainException {

    public GroupNotFoundException(Long id) {
        super("Group not found with id: " + id);
    }

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
