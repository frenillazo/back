package acainfo.back.enrollment.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when trying to create a duplicate group request.
 * Only one pending request per subject is allowed.
 */
public class DuplicateGroupRequestException extends DomainException {

    public DuplicateGroupRequestException(Long subjectId) {
        super("A pending group request already exists for subject with id: " + subjectId);
    }

    public DuplicateGroupRequestException(String message) {
        super(message);
    }

    public DuplicateGroupRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
