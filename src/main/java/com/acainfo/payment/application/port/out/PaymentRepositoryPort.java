package com.acainfo.payment.application.port.out;

import com.acainfo.payment.application.dto.PaymentFilters;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port for Payment persistence.
 * Defines the contract for payment repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface PaymentRepositoryPort {

    /**
     * Save or update a payment.
     *
     * @param payment Domain payment to persist
     * @return Persisted payment with ID
     */
    Payment save(Payment payment);

    /**
     * Save multiple payments.
     *
     * @param payments List of payments to persist
     * @return List of persisted payments
     */
    List<Payment> saveAll(List<Payment> payments);

    /**
     * Find payment by ID.
     *
     * @param id Payment ID
     * @return Optional containing the payment if found
     */
    Optional<Payment> findById(Long id);

    /**
     * Find payments with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of payments matching filters
     */
    Page<Payment> findWithFilters(PaymentFilters filters);

    /**
     * Find all payments for a student.
     *
     * @param studentId Student ID
     * @return List of payments
     */
    List<Payment> findByStudentId(Long studentId);

    /**
     * Find all payments for an enrollment.
     *
     * @param enrollmentId Enrollment ID
     * @return List of payments
     */
    List<Payment> findByEnrollmentId(Long enrollmentId);

    /**
     * Find payments by student and status.
     *
     * @param studentId Student ID
     * @param status Payment status
     * @return List of payments
     */
    List<Payment> findByStudentIdAndStatus(Long studentId, PaymentStatus status);

    /**
     * Find payments by enrollment and billing period.
     *
     * @param enrollmentId Enrollment ID
     * @param billingMonth Billing month
     * @param billingYear Billing year
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByEnrollmentIdAndBillingPeriod(Long enrollmentId, Integer billingMonth, Integer billingYear);

    /**
     * Find payments by enrollment and type.
     *
     * @param enrollmentId Enrollment ID
     * @param type Payment type
     * @return List of payments
     */
    List<Payment> findByEnrollmentIdAndType(Long enrollmentId, PaymentType type);

    /**
     * Check if a payment exists for enrollment and billing period.
     *
     * @param enrollmentId Enrollment ID
     * @param billingMonth Billing month
     * @param billingYear Billing year
     * @return true if payment exists
     */
    boolean existsByEnrollmentIdAndBillingPeriod(Long enrollmentId, Integer billingMonth, Integer billingYear);

    /**
     * Check if an INTENSIVE_FULL payment exists for enrollment.
     *
     * @param enrollmentId Enrollment ID
     * @return true if intensive payment exists
     */
    boolean existsIntensivePaymentByEnrollmentId(Long enrollmentId);

    /**
     * Find overdue payments for a student.
     * Overdue = PENDING status and dueDate < today.
     *
     * @param studentId Student ID
     * @param today Current date for comparison
     * @return List of overdue payments
     */
    List<Payment> findOverdueByStudentId(Long studentId, LocalDate today);

    /**
     * Find all overdue payments in the system.
     *
     * @param today Current date for comparison
     * @return List of all overdue payments
     */
    List<Payment> findAllOverdue(LocalDate today);

    /**
     * Check if student has any overdue payments.
     *
     * @param studentId Student ID
     * @param today Current date for comparison
     * @return true if student has overdue payments
     */
    boolean hasOverduePayments(Long studentId, LocalDate today);

    /**
     * Count pending payments for a student.
     *
     * @param studentId Student ID
     * @return Number of pending payments
     */
    long countPendingByStudentId(Long studentId);

    /**
     * Delete a payment.
     *
     * @param id Payment ID
     */
    void delete(Long id);
}
