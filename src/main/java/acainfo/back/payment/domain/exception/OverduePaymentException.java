package acainfo.back.payment.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when a student has overdue payments blocking their access.
 */
public class OverduePaymentException extends DomainException {

    public OverduePaymentException() {
        super("Student has overdue payments. Please pay pending fees to continue.");
    }

    public OverduePaymentException(Long studentId, int overdueCount) {
        super(String.format(
            "Student %d has %d overdue payment(s). Access blocked until payments are made.",
            studentId, overdueCount
        ));
    }

    public OverduePaymentException(String message) {
        super(message);
    }

    public OverduePaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
