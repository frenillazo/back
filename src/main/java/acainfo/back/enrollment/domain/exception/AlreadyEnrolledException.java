package acainfo.back.enrollment.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when a student tries to enroll in a group they are already enrolled in.
 */
public class AlreadyEnrolledException extends DomainException {

    public AlreadyEnrolledException(Long studentId, Long groupId) {
        super("Student with id " + studentId + " is already enrolled in group with id " + groupId);
    }

    public AlreadyEnrolledException(String message) {
        super(message);
    }

    public AlreadyEnrolledException(String message, Throwable cause) {
        super(message, cause);
    }
}
