package com.acainfo.payment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when payment amount cannot be calculated.
 */
public class PaymentCalculationException extends BusinessRuleException {

    public PaymentCalculationException(String message) {
        super(message);
    }

    public PaymentCalculationException(Long enrollmentId, String reason) {
        super("Cannot calculate payment for enrollment " + enrollmentId + ": " + reason);
    }

    @Override
    public String getErrorCode() {
        return "PAYMENT_CALCULATION_ERROR";
    }
}
