package com.acainfo.payment.application.port.in;

import com.acainfo.payment.application.dto.GeneratePaymentCommand;
import com.acainfo.payment.domain.model.Payment;

/**
 * Use case for generating a payment for an enrollment.
 * Input port defining the contract for payment generation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>INITIAL: Generated on enrollment, covers remaining sessions of current month</li>
 *   <li>MONTHLY: Generated on 1st, covers all sessions of that month</li>
 *   <li>INTENSIVE_FULL: Generated on enrollment, covers all sessions</li>
 *   <li>Amount = sessions × hours × pricePerHour (from enrollment)</li>
 *   <li>dueDate = generatedAt + 5 days</li>
 * </ul>
 */
public interface GeneratePaymentUseCase {

    /**
     * Generate a payment for an enrollment.
     *
     * @param command Payment generation data
     * @return The generated payment
     * @throws com.acainfo.payment.domain.exception.PaymentAlreadyExistsException if payment already exists for period
     * @throws com.acainfo.payment.domain.exception.PaymentCalculationException if amount cannot be calculated
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if enrollment not found
     */
    Payment generate(GeneratePaymentCommand command);
}
