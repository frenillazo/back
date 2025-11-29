package acainfo.back.payment.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when attempting to pay an already paid payment.
 */
public class PaymentAlreadyPaidException extends DomainException {

    public PaymentAlreadyPaidException(Long paymentId) {
        super("Payment with id " + paymentId + " is already paid");
    }

    public PaymentAlreadyPaidException(String message) {
        super(message);
    }

    public PaymentAlreadyPaidException(String message, Throwable cause) {
        super(message, cause);
    }
}
