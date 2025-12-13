package com.acainfo.payment.application.port.in;

/**
 * Use case for checking payment status and access permissions.
 * Input port defining the contract for payment status checks.
 *
 * <p>Used by other modules to verify if a student has access
 * based on their payment status.</p>
 */
public interface CheckPaymentStatusUseCase {

    /**
     * Check if a student has any overdue payments that block access.
     *
     * @param studentId Student ID
     * @return true if student has overdue payments
     */
    boolean hasOverduePayments(Long studentId);

    /**
     * Check if a student can access protected resources.
     * Returns false if student has overdue payments.
     *
     * @param studentId Student ID
     * @return true if student can access resources
     */
    boolean canAccessResources(Long studentId);

    /**
     * Check if student is up to date with all payments.
     *
     * @param studentId Student ID
     * @return true if no pending or overdue payments
     */
    boolean isUpToDate(Long studentId);
}
