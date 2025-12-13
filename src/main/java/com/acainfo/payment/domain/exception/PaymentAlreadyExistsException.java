package com.acainfo.payment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to create a duplicate payment.
 */
public class PaymentAlreadyExistsException extends BusinessRuleException {

    public PaymentAlreadyExistsException(Long enrollmentId, Integer billingMonth, Integer billingYear) {
        super("Payment already exists for enrollment " + enrollmentId +
              " for period " + billingYear + "-" + String.format("%02d", billingMonth));
    }

    public PaymentAlreadyExistsException(Long enrollmentId, String type) {
        super("Payment of type " + type + " already exists for enrollment " + enrollmentId);
    }

    @Override
    public String getErrorCode() {
        return "PAYMENT_ALREADY_EXISTS";
    }
}
