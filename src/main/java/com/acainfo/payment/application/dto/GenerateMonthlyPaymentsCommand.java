package com.acainfo.payment.application.dto;

/**
 * Command to generate monthly payments for all active REGULAR enrollments.
 * Used by scheduled job on the 1st of each month.
 *
 * @param billingMonth Month to bill (1-12)
 * @param billingYear Year to bill
 */
public record GenerateMonthlyPaymentsCommand(
        Integer billingMonth,
        Integer billingYear
) {
}
