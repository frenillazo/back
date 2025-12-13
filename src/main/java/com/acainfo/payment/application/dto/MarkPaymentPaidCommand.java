package com.acainfo.payment.application.dto;

import java.time.LocalDateTime;

/**
 * Command to mark a payment as paid.
 *
 * @param paymentId ID of the payment
 * @param paidAt Timestamp when payment was received (null for now)
 * @param stripePaymentIntentId Optional Stripe payment intent ID
 */
public record MarkPaymentPaidCommand(
        Long paymentId,
        LocalDateTime paidAt,
        String stripePaymentIntentId
) {
    /**
     * Constructor for manual payment without Stripe.
     */
    public MarkPaymentPaidCommand(Long paymentId) {
        this(paymentId, LocalDateTime.now(), null);
    }
}
