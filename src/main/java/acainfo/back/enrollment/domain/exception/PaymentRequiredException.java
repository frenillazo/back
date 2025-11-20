package acainfo.back.enrollment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when a student with overdue payments tries to enroll or access resources.
 */
public class PaymentRequiredException extends DomainException {

    public PaymentRequiredException(Long studentId) {
        super("Student with id " + studentId + " has overdue payments and cannot enroll");
    }

    public PaymentRequiredException(String message) {
        super(message);
    }

    public PaymentRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
