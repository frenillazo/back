package acainfo.back.subjectgroup.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when a subjectGroup is not found.
 */
public class GroupNotFoundException extends DomainException {

    public GroupNotFoundException(Long id) {
        super("SubjectGroup not found with id: " + id);
    }

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
