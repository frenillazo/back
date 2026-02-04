package com.acainfo.payment.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * REST request DTO for generating payments for all active enrollments of a group.
 */
public record GenerateGroupPaymentsRequest(
        @NotNull(message = "Group ID is required")
        Long groupId,

        @NotNull(message = "Billing month is required")
        @Min(value = 1, message = "Billing month must be between 1 and 12")
        @Max(value = 12, message = "Billing month must be between 1 and 12")
        Integer billingMonth,

        @NotNull(message = "Billing year is required")
        @Min(value = 2020, message = "Billing year must be 2020 or later")
        Integer billingYear,

        @Positive(message = "Custom amount must be positive")
        BigDecimal customAmount
) {
}
