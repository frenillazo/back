package com.acainfo.payment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when access is blocked due to overdue payment.
 */
public class PaymentOverdueException extends BusinessRuleException {

    public PaymentOverdueException(Long studentId) {
        super("Access blocked for student " + studentId + " due to overdue payment");
    }

    public PaymentOverdueException(Long studentId, Long paymentId) {
        super("Access blocked for student " + studentId + " due to overdue payment " + paymentId);
    }

    @Override
    public String getErrorCode() {
        return "PAYMENT_OVERDUE";
    }
}
