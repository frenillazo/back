package acainfo.back.payment.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pure Domain Model for Payment.
 * Represents a payment made by a student.
 * Immutable and contains only business logic.
 *
 * Business Rules:
 * - Students with overdue payments (>5 days) cannot access materials
 * - Students with overdue payments cannot enroll in new groups
 * - Refunds are calculated proportionally based on withdrawal date
 * - Monthly payments are generated automatically on the 1st of each month
 * - Intensive course payments are one-time
 */
@Value
@Builder(toBuilder = true)
public class PaymentDomain {

    Long id;

    /**
     * ID of the student who must make this payment
     */
    Long studentId;

    /**
     * Payment amount in EUR
     */
    BigDecimal amount;

    /**
     * Type of payment (monthly, intensive, etc.)
     */
    PaymentType paymentType;

    /**
     * Current status of the payment
     */
    @With
    PaymentStatus status;

    /**
     * Date when payment is due
     */
    LocalDate dueDate;

    /**
     * Date when payment was completed (null if not paid yet)
     */
    @With
    LocalDate paidDate;

    /**
     * Stripe payment intent ID for tracking the transaction
     */
    @With
    String stripePaymentId;

    /**
     * Internal invoice number for accounting
     */
    @With
    String invoiceNumber;

    /**
     * Optional description or notes about the payment
     */
    @With
    String description;

    /**
     * Academic period this payment corresponds to (e.g., "2024-Q1")
     */
    String academicPeriod;

    /**
     * Audit: when the payment record was created
     */
    LocalDateTime createdAt;

    /**
     * Audit: when the payment record was last updated
     */
    LocalDateTime updatedAt;

    /**
     * Audit: ID of user who processed the payment
     */
    @With
    Long processedBy;

    /**
     * Version for optimistic locking
     */
    Long version;

    // ==================== Business Methods ====================

    /**
     * Mark payment as paid and record the payment date
     */
    public PaymentDomain markAsPaid(Long processedByUserId) {
        validateTransition(PaymentStatus.PAGADO);
        return this.toBuilder()
                .status(PaymentStatus.PAGADO)
                .paidDate(LocalDate.now())
                .processedBy(processedByUserId)
                .build();
    }

    /**
     * Mark payment as overdue (>5 days past due date)
     */
    public PaymentDomain markAsOverdue() {
        if (this.status == PaymentStatus.PENDIENTE) {
            return this.withStatus(PaymentStatus.ATRASADO);
        }
        return this;
    }

    /**
     * Cancel the payment
     */
    public PaymentDomain cancel(Long processedByUserId, String reason) {
        validateTransition(PaymentStatus.CANCELADO);

        String newDescription = (this.description != null ? this.description + " | " : "")
                + "Cancelled: " + reason;

        return this.toBuilder()
                .status(PaymentStatus.CANCELADO)
                .processedBy(processedByUserId)
                .description(newDescription)
                .build();
    }

    /**
     * Process a refund for this payment
     */
    public PaymentDomain refund(Long processedByUserId, String reason) {
        if (this.status != PaymentStatus.PAGADO) {
            throw new IllegalStateException("Cannot refund a payment that is not PAID");
        }

        String newDescription = (this.description != null ? this.description + " | " : "")
                + "Refunded: " + reason;

        return this.toBuilder()
                .status(PaymentStatus.REEMBOLSADO)
                .processedBy(processedByUserId)
                .description(newDescription)
                .build();
    }

    /**
     * Check if the payment is overdue (>5 days past due date)
     */
    public boolean isOverdue() {
        if (this.status == PaymentStatus.ATRASADO) {
            return true;
        }
        if (this.status == PaymentStatus.PENDIENTE && this.dueDate != null) {
            return LocalDate.now().isAfter(this.dueDate.plusDays(5));
        }
        return false;
    }

    /**
     * Get the number of days overdue (negative if not yet due)
     */
    public long getDaysOverdue() {
        if (this.dueDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, LocalDate.now());
    }

    /**
     * Check if this payment blocks student access (overdue >5 days)
     */
    public boolean blocksAccess() {
        return isOverdue() && getDaysOverdue() > 5;
    }

    /**
     * Generate invoice number if not set
     */
    public PaymentDomain generateInvoiceNumber() {
        if (this.invoiceNumber == null) {
            // Format: INV-YYYYMMDD-STUDENTID-RANDOM
            String date = LocalDate.now().toString().replace("-", "");
            String studentId = String.valueOf(this.studentId);
            String random = String.valueOf((int)(Math.random() * 10000));
            String newInvoiceNumber = String.format("INV-%s-%s-%s", date, studentId, random);
            return this.withInvoiceNumber(newInvoiceNumber);
        }
        return this;
    }

    /**
     * Validate business rules
     */
    public void validate() {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required");
        }
    }

    // ==================== Helper Methods ====================

    private void validateTransition(PaymentStatus newStatus) {
        if (this.status == newStatus) {
            throw new IllegalStateException(
                String.format("Payment is already in status %s", newStatus)
            );
        }

        // Validate allowed transitions
        switch (newStatus) {
            case PAGADO:
                if (this.status != PaymentStatus.PENDIENTE &&
                    this.status != PaymentStatus.ATRASADO) {
                    throw new IllegalStateException(
                        String.format("Cannot mark payment as PAID from status %s", this.status)
                    );
                }
                break;
            case CANCELADO:
                if (this.status == PaymentStatus.PAGADO ||
                    this.status == PaymentStatus.REEMBOLSADO) {
                    throw new IllegalStateException(
                        String.format("Cannot cancel payment in status %s", this.status)
                    );
                }
                break;
        }
    }
}
