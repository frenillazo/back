package com.acainfo.payment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment domain entity - Anemic model with Lombok.
 * Represents a payment generated for an enrollment.
 *
 * <p>Payment generation rules:</p>
 * <ul>
 *   <li>REGULAR groups - INITIAL: Generated on enrollment, covers remaining sessions of current month</li>
 *   <li>REGULAR groups - MONTHLY: Generated on 1st of each month, covers all sessions of that month</li>
 *   <li>INTENSIVE groups - INTENSIVE_FULL: Single payment on enrollment, covers all sessions</li>
 * </ul>
 *
 * <p>Pricing:</p>
 * <ul>
 *   <li>Price per hour is stored in each Enrollment (allows personalized pricing)</li>
 *   <li>Amount = number of sessions × duration in hours × €/hour</li>
 *   <li>pricePerHour is snapshotted at payment generation for audit purposes</li>
 * </ul>
 *
 * <p>Access blocking:</p>
 * <ul>
 *   <li>Students with overdue payments (5+ days past dueDate) are blocked from:</li>
 *   <li>Materials, schedules, sessions, notifications, reservations</li>
 *   <li>They can only access: profile, public info, academic offer, pricing</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Payment {

    private static final int DAYS_UNTIL_OVERDUE = 5;

    private Long id;

    /**
     * Reference to the enrollment this payment belongs to.
     * Each payment is tied to ONE enrollment.
     */
    private Long enrollmentId;

    /**
     * Reference to the student (denormalized for fast queries).
     */
    private Long studentId;

    /**
     * Type of payment: INITIAL, MONTHLY, or INTENSIVE_FULL.
     */
    private PaymentType type;

    /**
     * Current status of the payment.
     */
    private PaymentStatus status;

    /**
     * Calculated amount to pay in euros.
     */
    private BigDecimal amount;

    /**
     * Total hours billed (for audit/transparency).
     */
    private BigDecimal totalHours;

    /**
     * Price per hour applied (snapshot from enrollment at generation time).
     */
    private BigDecimal pricePerHour;

    /**
     * Billing month (1-12).
     * For REGULAR: the month being billed.
     * For INTENSIVE: the month when the intensive course starts.
     */
    private Integer billingMonth;

    /**
     * Billing year.
     */
    private Integer billingYear;

    /**
     * Date when the payment was generated.
     */
    private LocalDate generatedAt;

    /**
     * Due date for payment (generatedAt + 5 days).
     */
    private LocalDate dueDate;

    /**
     * Timestamp when the payment was marked as paid.
     * Null if not yet paid.
     */
    private LocalDateTime paidAt;

    /**
     * Optional description or notes.
     */
    private String description;

    /**
     * Stripe Payment Intent ID (for future Stripe integration).
     */
    private String stripePaymentIntentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Status Query Methods ====================

    /**
     * Check if payment is pending.
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    /**
     * Check if payment is paid.
     */
    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    /**
     * Check if payment is cancelled.
     */
    public boolean isCancelled() {
        return status == PaymentStatus.CANCELLED;
    }

    // ==================== Computed Properties ====================

    /**
     * Check if payment is overdue (past due date and still pending).
     * This is a calculated state, not a stored status.
     */
    public boolean isOverdue() {
        return isPending() && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Check if this payment should block student access.
     * Same as isOverdue() - overdue payments block access.
     */
    public boolean shouldBlockAccess() {
        return isOverdue();
    }

    /**
     * Get the number of days overdue (0 if not overdue).
     */
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    /**
     * Check if payment is for a regular group (INITIAL or MONTHLY).
     */
    public boolean isForRegularGroup() {
        return type == PaymentType.INITIAL || type == PaymentType.MONTHLY;
    }

    /**
     * Check if payment is for an intensive group.
     */
    public boolean isForIntensiveGroup() {
        return type == PaymentType.INTENSIVE_FULL;
    }

    /**
     * Get billing period as string (e.g., "2025-01" or "Enero 2025").
     */
    public String getBillingPeriod() {
        if (billingMonth == null || billingYear == null) {
            return null;
        }
        return String.format("%d-%02d", billingYear, billingMonth);
    }
}
