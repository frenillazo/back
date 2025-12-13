package com.acainfo.payment.infrastructure.adapter.in.rest.dto;

import com.acainfo.payment.domain.model.PaymentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * REST request DTO for generating a payment.
 */
public record GeneratePaymentRequest(
        @NotNull(message = "Enrollment ID is required")
        Long enrollmentId,

        @NotNull(message = "Payment type is required")
        PaymentType type,

        @NotNull(message = "Billing month is required")
        @Min(value = 1, message = "Billing month must be between 1 and 12")
        @Max(value = 12, message = "Billing month must be between 1 and 12")
        Integer billingMonth,

        @NotNull(message = "Billing year is required")
        @Min(value = 2020, message = "Billing year must be 2020 or later")
        Integer billingYear
) {
}
