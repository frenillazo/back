package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when an enrollment cannot be cancelled.
 */
public class EnrollmentCannotBeCancelledException extends DomainException {

    public EnrollmentCannotBeCancelledException(Long id, String reason) {
        super("Enrollment " + id + " cannot be cancelled: " + reason);
    }

    public EnrollmentCannotBeCancelledException(String message) {
        super(message);
    }

    public EnrollmentCannotBeCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
