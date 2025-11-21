package acainfo.back.payment.infrastructure.adapters.out;

import acainfo.back.payment.domain.model.Payment;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.domain.model.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Payment entity.
 * Provides CRUD operations and custom queries for payment management.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments by student ID
     */
    @Query("SELECT p FROM Payment p WHERE p.student.id = :studentId ORDER BY p.dueDate DESC")
    List<Payment> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find payments by student ID and status
     */
    @Query("SELECT p FROM Payment p WHERE p.student.id = :studentId AND p.status = :status ORDER BY p.dueDate DESC")
    List<Payment> findByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") PaymentStatus status
    );

    /**
     * Find all payments with a specific status
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.dueDate")
    List<Payment> findByStatus(@Param("status") PaymentStatus status);

    /**
     * Find overdue payments (PENDIENTE status and due date passed by more than 5 days)
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDIENTE' AND p.dueDate < :date")
    List<Payment> findOverduePayments(@Param("date") LocalDate date);

    /**
     * Find all pending or overdue payments for a student
     */
    @Query("SELECT p FROM Payment p WHERE p.student.id = :studentId " +
           "AND (p.status = 'PENDIENTE' OR p.status = 'ATRASADO') " +
           "ORDER BY p.dueDate ASC")
    List<Payment> findPendingPaymentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Check if student has any overdue payments (ATRASADO status or PENDIENTE >5 days)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p " +
           "WHERE p.student.id = :studentId AND " +
           "(p.status = 'ATRASADO' OR (p.status = 'PENDIENTE' AND p.dueDate < :fiveDaysAgo))")
    boolean hasOverduePayments(
        @Param("studentId") Long studentId,
        @Param("fiveDaysAgo") LocalDate fiveDaysAgo
    );

    /**
     * Check if student has any blocking payments (overdue >5 days)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p " +
           "WHERE p.student.id = :studentId AND p.status = 'ATRASADO' AND p.dueDate < :fiveDaysAgo")
    boolean hasBlockingPayments(
        @Param("studentId") Long studentId,
        @Param("fiveDaysAgo") LocalDate fiveDaysAgo
    );

    /**
     * Count overdue payments for a student
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.student.id = :studentId " +
           "AND (p.status = 'ATRASADO' OR (p.status = 'PENDIENTE' AND p.dueDate < :date))")
    long countOverduePaymentsByStudentId(
        @Param("studentId") Long studentId,
        @Param("date") LocalDate date
    );

    /**
     * Find payments by student and payment type
     */
    @Query("SELECT p FROM Payment p WHERE p.student.id = :studentId AND p.paymentType = :type " +
           "ORDER BY p.dueDate DESC")
    List<Payment> findByStudentIdAndPaymentType(
        @Param("studentId") Long studentId,
        @Param("type") PaymentType type
    );

    /**
     * Find payments by academic period
     */
    @Query("SELECT p FROM Payment p WHERE p.academicPeriod = :period ORDER BY p.student.id, p.dueDate")
    List<Payment> findByAcademicPeriod(@Param("period") String period);

    /**
     * Find payments by status and academic period
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.academicPeriod = :period " +
           "ORDER BY p.dueDate")
    List<Payment> findByStatusAndAcademicPeriod(
        @Param("status") PaymentStatus status,
        @Param("period") String period
    );

    /**
     * Find payment by Stripe payment ID
     */
    @Query("SELECT p FROM Payment p WHERE p.stripePaymentId = :stripePaymentId")
    Optional<Payment> findByStripePaymentId(@Param("stripePaymentId") String stripePaymentId);

    /**
     * Find payment by invoice number
     */
    @Query("SELECT p FROM Payment p WHERE p.invoiceNumber = :invoiceNumber")
    Optional<Payment> findByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);

    /**
     * Find payments due between two dates
     */
    @Query("SELECT p FROM Payment p WHERE p.dueDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.dueDate, p.student.id")
    List<Payment> findPaymentsDueBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find paid payments between two dates (for reporting)
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PAGADO' " +
           "AND p.paidDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.paidDate")
    List<Payment> findPaidPaymentsBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total revenue between two dates
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAGADO' " +
           "AND p.paidDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateRevenueBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total pending amount for a student
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.student.id = :studentId " +
           "AND (p.status = 'PENDIENTE' OR p.status = 'ATRASADO')")
    java.math.BigDecimal calculateTotalPendingByStudentId(@Param("studentId") Long studentId);

    /**
     * Count payments by status
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);

    /**
     * Find students with overdue payments (for notification/blocking)
     */
    @Query("SELECT DISTINCT p.student.id FROM Payment p WHERE p.status = 'ATRASADO'")
    List<Long> findStudentIdsWithOverduePayments();

    // ==================== Default Methods ====================

    /**
     * Find all pending payments
     */
    default List<Payment> findAllPending() {
        return findByStatus(PaymentStatus.PENDIENTE);
    }

    /**
     * Find all overdue payments (ATRASADO status)
     */
    default List<Payment> findAllOverdue() {
        return findByStatus(PaymentStatus.ATRASADO);
    }

    /**
     * Find all paid payments
     */
    default List<Payment> findAllPaid() {
        return findByStatus(PaymentStatus.PAGADO);
    }

    /**
     * Check if student has any active (pending or overdue) payments
     */
    default boolean hasActivePayments(Long studentId) {
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
        return hasOverduePayments(studentId, fiveDaysAgo);
    }

    /**
     * Get all overdue payments that should be marked as ATRASADO
     */
    default List<Payment> findPaymentsToMarkAsOverdue() {
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
        return findOverduePayments(fiveDaysAgo);
    }
}
