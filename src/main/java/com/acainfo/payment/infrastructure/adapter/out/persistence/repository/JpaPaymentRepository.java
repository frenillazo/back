package com.acainfo.payment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import com.acainfo.payment.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Payment entities.
 */
@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentJpaEntity, Long>,
        JpaSpecificationExecutor<PaymentJpaEntity> {

    /**
     * Find all payments for a student.
     */
    List<PaymentJpaEntity> findByStudentIdOrderByDueDateDesc(Long studentId);

    /**
     * Find all payments for an enrollment.
     */
    List<PaymentJpaEntity> findByEnrollmentIdOrderByBillingYearDescBillingMonthDesc(Long enrollmentId);

    /**
     * Find payments by student and status.
     */
    List<PaymentJpaEntity> findByStudentIdAndStatusOrderByDueDateAsc(Long studentId, PaymentStatus status);

    /**
     * Find payment by enrollment, billing period, and type.
     */
    Optional<PaymentJpaEntity> findByEnrollmentIdAndBillingMonthAndBillingYearAndType(
            Long enrollmentId, Integer billingMonth, Integer billingYear, PaymentType type);

    /**
     * Check if payment exists for enrollment, billing period, and type.
     */
    boolean existsByEnrollmentIdAndBillingMonthAndBillingYearAndType(
            Long enrollmentId, Integer billingMonth, Integer billingYear, PaymentType type);

    /**
     * Find overdue payments (PENDING with due date before today minus grace period).
     * Uses native query for date arithmetic compatibility.
     */
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.status = :status AND p.dueDate < :overdueDate ORDER BY p.dueDate ASC")
    List<PaymentJpaEntity> findOverduePayments(
            @Param("status") PaymentStatus status,
            @Param("overdueDate") LocalDate overdueDate);

    /**
     * Find overdue payments for a specific student.
     */
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.studentId = :studentId AND p.status = :status AND p.dueDate < :overdueDate ORDER BY p.dueDate ASC")
    List<PaymentJpaEntity> findOverduePaymentsByStudentId(
            @Param("studentId") Long studentId,
            @Param("status") PaymentStatus status,
            @Param("overdueDate") LocalDate overdueDate);

    /**
     * Check if student has any overdue payments (for access blocking).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PaymentJpaEntity p WHERE p.studentId = :studentId AND p.status = :status AND p.dueDate < :overdueDate")
    boolean hasOverduePayments(
            @Param("studentId") Long studentId,
            @Param("status") PaymentStatus status,
            @Param("overdueDate") LocalDate overdueDate);

    /**
     * Find pending payments for a student ordered by due date.
     */
    List<PaymentJpaEntity> findByStudentIdAndStatusOrderByDueDateAsc(Long studentId, String status);

    /**
     * Count payments by enrollment.
     */
    long countByEnrollmentId(Long enrollmentId);

    /**
     * Find all payments for multiple enrollments.
     */
    List<PaymentJpaEntity> findByEnrollmentIdIn(List<Long> enrollmentIds);
}
