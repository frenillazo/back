package com.acainfo.payment.application.port.in;

import com.acainfo.payment.application.dto.MarkPaymentPaidCommand;
import com.acainfo.payment.domain.model.Payment;

/**
 * Use case for marking a payment as paid.
 * Input port defining the contract for payment completion.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only PENDING payments can be marked as paid</li>
 *   <li>Sets paidAt timestamp and status to PAID</li>
 *   <li>Optionally stores Stripe payment intent ID</li>
 * </ul>
 */
public interface MarkPaymentPaidUseCase {

    /**
     * Mark a payment as paid.
     *
     * @param command Payment completion data
     * @return The updated payment
     * @throws com.acainfo.payment.domain.exception.PaymentNotFoundException if payment not found
     * @throws com.acainfo.payment.domain.exception.InvalidPaymentStateException if not pending
     */
    Payment markAsPaid(MarkPaymentPaidCommand command);
}
