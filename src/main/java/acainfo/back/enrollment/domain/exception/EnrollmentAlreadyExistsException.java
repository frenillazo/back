package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when trying to create a duplicate enrollment.
 */
public class EnrollmentAlreadyExistsException extends DomainException {

    public EnrollmentAlreadyExistsException(Long studentId, Long subjectGroupId) {
        super("Enrollment already exists for student " + studentId + " in group " + subjectGroupId);
    }

    public EnrollmentAlreadyExistsException(String message) {
        super(message);
    }

    public EnrollmentAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
