package com.acainfo.payment.application.port.in;

import com.acainfo.payment.application.dto.GenerateGroupPaymentsCommand;
import com.acainfo.payment.application.dto.GroupPaymentPreviewResponse;
import com.acainfo.payment.domain.model.Payment;

import java.util.List;

/**
 * Use case for generating payments for all active enrollments of a specific group.
 * Input port defining the contract for group-level payment generation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only generates for ACTIVE enrollments in the specified group</li>
 *   <li>Skips enrollments that already have a payment for that period</li>
 *   <li>Payment type is determined by group type (MONTHLY for regular, INTENSIVE_FULL for intensive)</li>
 *   <li>Admin can override the calculated amount with a custom amount</li>
 * </ul>
 */
public interface GenerateGroupPaymentsUseCase {

    /**
     * Preview payments that would be generated for a group.
     * Shows calculated amounts for each enrollment without creating payments.
     *
     * @param groupId Group ID
     * @param billingMonth Billing month (1-12)
     * @param billingYear Billing year
     * @return Preview with calculated amounts for each enrollment
     */
    GroupPaymentPreviewResponse preview(Long groupId, Integer billingMonth, Integer billingYear);

    /**
     * Generate payments for all active enrollments in a group.
     *
     * @param command Group ID, billing period, and optional custom amount
     * @return List of generated payments
     */
    List<Payment> generate(GenerateGroupPaymentsCommand command);
}
