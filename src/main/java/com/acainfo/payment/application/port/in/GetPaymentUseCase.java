package com.acainfo.payment.application.port.in;

import com.acainfo.payment.application.dto.PaymentFilters;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.shared.application.dto.PageResponse;

import java.util.List;

/**
 * Use case for querying payments.
 * Input port defining the contract for payment queries.
 */
public interface GetPaymentUseCase {

    /**
     * Get a payment by ID.
     *
     * @param id Payment ID
     * @return The payment
     * @throws com.acainfo.payment.domain.exception.PaymentNotFoundException if not found
     */
    Payment getById(Long id);

    /**
     * Find payments with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of payments matching filters
     */
    PageResponse<Payment> findWithFilters(PaymentFilters filters);

    /**
     * Get all payments for a student.
     *
     * @param studentId Student ID
     * @return List of payments
     */
    List<Payment> getByStudentId(Long studentId);

    /**
     * Get all payments for an enrollment.
     *
     * @param enrollmentId Enrollment ID
     * @return List of payments
     */
    List<Payment> getByEnrollmentId(Long enrollmentId);

    /**
     * Get pending payments for a student.
     *
     * @param studentId Student ID
     * @return List of pending payments
     */
    List<Payment> getPendingByStudentId(Long studentId);

    /**
     * Get overdue payments for a student.
     *
     * @param studentId Student ID
     * @return List of overdue payments
     */
    List<Payment> getOverdueByStudentId(Long studentId);

    /**
     * Get all overdue payments (admin view).
     *
     * @return List of all overdue payments
     */
    List<Payment> getAllOverdue();
}
