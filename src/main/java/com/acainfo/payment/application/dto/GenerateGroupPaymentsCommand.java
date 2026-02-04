package com.acainfo.payment.application.dto;

import java.math.BigDecimal;

/**
 * Command to generate payments for all active enrollments of a group.
 *
 * @param groupId ID of the group
 * @param billingMonth Billing month (1-12)
 * @param billingYear Billing year
 * @param customAmount Optional custom amount to apply to all payments (null = use calculated)
 */
public record GenerateGroupPaymentsCommand(
        Long groupId,
        Integer billingMonth,
        Integer billingYear,
        BigDecimal customAmount
) {
}
