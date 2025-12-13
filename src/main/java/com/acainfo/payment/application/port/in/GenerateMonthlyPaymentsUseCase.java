package com.acainfo.payment.application.port.in;

import com.acainfo.payment.application.dto.GenerateMonthlyPaymentsCommand;
import com.acainfo.payment.domain.model.Payment;

import java.util.List;

/**
 * Use case for generating monthly payments for all active REGULAR enrollments.
 * Input port defining the contract for batch monthly payment generation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Called by scheduled job on the 1st of each month</li>
 *   <li>Only generates for ACTIVE enrollments in REGULAR groups</li>
 *   <li>Skips enrollments that already have a payment for that month</li>
 *   <li>Skips groups that end before the billing month</li>
 * </ul>
 */
public interface GenerateMonthlyPaymentsUseCase {

    /**
     * Generate monthly payments for all eligible enrollments.
     *
     * @param command Billing period (month, year)
     * @return List of generated payments
     */
    List<Payment> generateMonthlyPayments(GenerateMonthlyPaymentsCommand command);
}
