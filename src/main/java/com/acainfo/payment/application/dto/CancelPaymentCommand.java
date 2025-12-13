package com.acainfo.payment.application.dto;

/**
 * Command to cancel a payment.
 * Used when a student withdraws from enrollment.
 *
 * @param paymentId ID of the payment to cancel
 * @param reason Optional reason for cancellation
 */
public record CancelPaymentCommand(
        Long paymentId,
        String reason
) {
    public CancelPaymentCommand(Long paymentId) {
        this(paymentId, null);
    }
}
