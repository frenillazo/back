package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when an enrollment is not found.
 */
public class EnrollmentNotFoundException extends DomainException {

    public EnrollmentNotFoundException(Long id) {
        super("Enrollment not found with id: " + id);
    }

    public EnrollmentNotFoundException(String message) {
        super(message);
    }

    public EnrollmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
