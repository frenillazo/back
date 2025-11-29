package acainfo.back.enrollment.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when a group request is not found.
 */
public class GroupRequestNotFoundException extends DomainException {

    public GroupRequestNotFoundException(Long id) {
        super("Group request not found with id: " + id);
    }

    public GroupRequestNotFoundException(String message) {
        super(message);
    }

    public GroupRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
