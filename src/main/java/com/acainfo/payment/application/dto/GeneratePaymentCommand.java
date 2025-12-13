package com.acainfo.payment.application.dto;

import com.acainfo.payment.domain.model.PaymentType;

/**
 * Command to generate a payment for an enrollment.
 * Used when a student enrolls or for monthly payment generation.
 *
 * @param enrollmentId ID of the enrollment
 * @param type Payment type: INITIAL, MONTHLY, or INTENSIVE_FULL
 * @param billingMonth Billing month (1-12)
 * @param billingYear Billing year
 */
public record GeneratePaymentCommand(
        Long enrollmentId,
        PaymentType type,
        Integer billingMonth,
        Integer billingYear
) {
}
