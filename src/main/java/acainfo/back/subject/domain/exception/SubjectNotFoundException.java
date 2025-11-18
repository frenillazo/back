package acainfo.back.subject.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when a subject is not found.
 */
public class SubjectNotFoundException extends DomainException {

    public SubjectNotFoundException(Long id) {
        super("Subject not found with id: " + id);
    }

    public SubjectNotFoundException(String code) {
        super("Subject not found with code: " + code);
    }

    public SubjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
