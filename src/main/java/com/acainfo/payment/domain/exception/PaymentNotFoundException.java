package com.acainfo.payment.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "PAYMENT_NOT_FOUND";
    }
}
