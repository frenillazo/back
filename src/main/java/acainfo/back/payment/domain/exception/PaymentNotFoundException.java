package acainfo.back.payment.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends DomainException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
