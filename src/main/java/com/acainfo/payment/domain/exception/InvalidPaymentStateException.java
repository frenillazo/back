package com.acainfo.payment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an operation is not valid for the current payment state.
 */
public class InvalidPaymentStateException extends BusinessRuleException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }

    public InvalidPaymentStateException(Long paymentId, String currentState, String operation) {
        super("Cannot " + operation + " payment " + paymentId + " in state " + currentState);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_PAYMENT_STATE";
    }
}
