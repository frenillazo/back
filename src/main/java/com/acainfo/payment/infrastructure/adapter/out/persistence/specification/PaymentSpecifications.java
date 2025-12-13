package com.acainfo.payment.infrastructure.adapter.out.persistence.specification;

import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import com.acainfo.payment.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Payment entity using Criteria Builder.
 * Enables dynamic query building with type-safe filters.
 */
public final class PaymentSpecifications {

    private PaymentSpecifications() {
        // Utility class
    }

    /**
     * Filter by student ID.
     */
    public static Specification<PaymentJpaEntity> hasStudentId(Long studentId) {
        return (root, query, cb) -> studentId == null ? null :
                cb.equal(root.get("studentId"), studentId);
    }

    /**
     * Filter by enrollment ID.
     */
    public static Specification<PaymentJpaEntity> hasEnrollmentId(Long enrollmentId) {
        return (root, query, cb) -> enrollmentId == null ? null :
                cb.equal(root.get("enrollmentId"), enrollmentId);
    }

    /**
     * Filter by payment status.
     */
    public static Specification<PaymentJpaEntity> hasStatus(PaymentStatus status) {
        return (root, query, cb) -> status == null ? null :
                cb.equal(root.get("status"), status);
    }

    /**
     * Filter by payment type.
     */
    public static Specification<PaymentJpaEntity> hasType(PaymentType type) {
        return (root, query, cb) -> type == null ? null :
                cb.equal(root.get("type"), type);
    }

    /**
     * Filter by billing month.
     */
    public static Specification<PaymentJpaEntity> hasBillingMonth(Integer billingMonth) {
        return (root, query, cb) -> billingMonth == null ? null :
                cb.equal(root.get("billingMonth"), billingMonth);
    }

    /**
     * Filter by billing year.
     */
    public static Specification<PaymentJpaEntity> hasBillingYear(Integer billingYear) {
        return (root, query, cb) -> billingYear == null ? null :
                cb.equal(root.get("billingYear"), billingYear);
    }

    /**
     * Filter by billing period (month and year).
     */
    public static Specification<PaymentJpaEntity> hasBillingPeriod(Integer month, Integer year) {
        return Specification.where(hasBillingMonth(month)).and(hasBillingYear(year));
    }

    /**
     * Filter payments with due date before specified date.
     */
    public static Specification<PaymentJpaEntity> dueDateBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? null :
                cb.lessThan(root.get("dueDate"), date);
    }

    /**
     * Filter payments with due date after specified date.
     */
    public static Specification<PaymentJpaEntity> dueDateAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? null :
                cb.greaterThanOrEqualTo(root.get("dueDate"), date);
    }

    /**
     * Filter payments with due date between dates (inclusive).
     */
    public static Specification<PaymentJpaEntity> dueDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(root.get("dueDate"), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("dueDate"), from);
            return cb.between(root.get("dueDate"), from, to);
        };
    }

    /**
     * Filter overdue payments (PENDING status with due date before grace period).
     */
    public static Specification<PaymentJpaEntity> isOverdue(int graceDays) {
        return (root, query, cb) -> {
            LocalDate overdueDate = LocalDate.now().minusDays(graceDays);
            return cb.and(
                    cb.equal(root.get("status"), PaymentStatus.PENDING),
                    cb.lessThan(root.get("dueDate"), overdueDate)
            );
        };
    }

    /**
     * Filter by generated date range.
     */
    public static Specification<PaymentJpaEntity> generatedBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(root.get("generatedAt"), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("generatedAt"), from);
            return cb.between(root.get("generatedAt"), from, to);
        };
    }

    /**
     * Filter by paid date range.
     */
    public static Specification<PaymentJpaEntity> paidBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(root.get("paidAt").as(LocalDate.class), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("paidAt").as(LocalDate.class), from);
            return cb.between(root.get("paidAt").as(LocalDate.class), from, to);
        };
    }

    /**
     * Build a combined specification from PaymentFilters.
     */
    public static Specification<PaymentJpaEntity> fromFilters(
            Long studentId,
            Long enrollmentId,
            PaymentStatus status,
            PaymentType type,
            Integer billingMonth,
            Integer billingYear,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            Boolean overdueOnly,
            int graceDays) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }

            if (enrollmentId != null) {
                predicates.add(cb.equal(root.get("enrollmentId"), enrollmentId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (billingMonth != null) {
                predicates.add(cb.equal(root.get("billingMonth"), billingMonth));
            }

            if (billingYear != null) {
                predicates.add(cb.equal(root.get("billingYear"), billingYear));
            }

            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }

            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            if (Boolean.TRUE.equals(overdueOnly)) {
                LocalDate overdueDate = LocalDate.now().minusDays(graceDays);
                predicates.add(cb.equal(root.get("status"), PaymentStatus.PENDING));
                predicates.add(cb.lessThan(root.get("dueDate"), overdueDate));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
