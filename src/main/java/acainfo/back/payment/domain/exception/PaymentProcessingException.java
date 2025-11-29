package acainfo.back.payment.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when payment processing fails (Stripe integration errors, etc.).
 */
public class PaymentProcessingException extends DomainException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentProcessingException(Long paymentId, String reason) {
        super(String.format("Failed to process payment %d: %s", paymentId, reason));
    }
}
