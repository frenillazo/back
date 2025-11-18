package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when an enrollment status transition is invalid.
 */
public class InvalidEnrollmentStatusException extends DomainException {

    public InvalidEnrollmentStatusException(String currentStatus, String newStatus) {
        super("Invalid enrollment status transition from " + currentStatus + " to " + newStatus);
    }

    public InvalidEnrollmentStatusException(String message) {
        super(message);
    }

    public InvalidEnrollmentStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
