package com.acainfo.payment.application.port.in;

import com.acainfo.payment.application.dto.CancelPaymentCommand;
import com.acainfo.payment.domain.model.Payment;

/**
 * Use case for cancelling a payment.
 * Input port defining the contract for payment cancellation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only PENDING payments can be cancelled</li>
 *   <li>Typically used when a student withdraws from enrollment</li>
 * </ul>
 */
public interface CancelPaymentUseCase {

    /**
     * Cancel a payment.
     *
     * @param command Cancellation data
     * @return The cancelled payment
     * @throws com.acainfo.payment.domain.exception.PaymentNotFoundException if payment not found
     * @throws com.acainfo.payment.domain.exception.InvalidPaymentStateException if not pending
     */
    Payment cancel(CancelPaymentCommand command);
}
