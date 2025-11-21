package acainfo.back.payment.domain.model;

import acainfo.back.shared.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a payment made by a student.
 * Controls payment lifecycle from creation to completion or cancellation.
 *
 * Business rules:
 * - Students with overdue payments (>5 days) cannot access materials
 * - Students with overdue payments cannot enroll in new groups
 * - Refunds are calculated proportionally based on withdrawal date
 * - Monthly payments are generated automatically on the 1st of each month
 * - Intensive course payments are one-time
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_student", columnList = "student_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_due_date", columnList = "due_date"),
        @Index(name = "idx_payment_student_status", columnList = "student_id, status"),
        @Index(name = "idx_payment_stripe", columnList = "stripe_payment_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student who must make this payment
     */
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * Payment amount in EUR
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Type of payment (monthly, intensive, etc.)
     */
    @NotNull(message = "Payment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    /**
     * Current status of the payment
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDIENTE;

    /**
     * Date when payment is due
     */
    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Date when payment was completed (null if not paid yet)
     */
    @Column(name = "paid_date")
    private LocalDate paidDate;

    /**
     * Stripe payment intent ID for tracking the transaction
     */
    @Column(name = "stripe_payment_id", length = 100)
    @Size(max = 100)
    private String stripePaymentId;

    /**
     * Internal invoice number for accounting
     */
    @Column(name = "invoice_number", length = 50, unique = true)
    @Size(max = 50)
    private String invoiceNumber;

    /**
     * Optional description or notes about the payment
     */
    @Column(length = 500)
    @Size(max = 500)
    private String description;

    /**
     * Academic period this payment corresponds to (e.g., "2024-Q1")
     */
    @Column(name = "academic_period", length = 20)
    @Size(max = 20)
    private String academicPeriod;

    /**
     * Audit: when the payment record was created
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit: when the payment record was last updated
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Audit: User who processed the payment (admin, system, etc.)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    /**
     * Version for optimistic locking
     */
    @Version
    private Long version;

    // ==================== Business Methods ====================

    /**
     * Mark payment as paid and record the payment date
     */
    public void markAsPaid(User processedBy) {
        validateTransition(PaymentStatus.PAGADO);
        this.status = PaymentStatus.PAGADO;
        this.paidDate = LocalDate.now();
        this.processedBy = processedBy;
    }

    /**
     * Mark payment as overdue (>5 days past due date)
     */
    public void markAsOverdue() {
        if (this.status == PaymentStatus.PENDIENTE) {
            this.status = PaymentStatus.ATRASADO;
        }
    }

    /**
     * Cancel the payment
     */
    public void cancel(User processedBy, String reason) {
        validateTransition(PaymentStatus.CANCELADO);
        this.status = PaymentStatus.CANCELADO;
        this.processedBy = processedBy;
        if (reason != null) {
            this.description = (this.description != null ? this.description + " | " : "")
                + "Cancelled: " + reason;
        }
    }

    /**
     * Process a refund for this payment
     */
    public void refund(User processedBy, String reason) {
        if (this.status != PaymentStatus.PAGADO) {
            throw new IllegalStateException("Cannot refund a payment that is not PAID");
        }
        this.status = PaymentStatus.REEMBOLSADO;
        this.processedBy = processedBy;
        if (reason != null) {
            this.description = (this.description != null ? this.description + " | " : "")
                + "Refunded: " + reason;
        }
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
    public void generateInvoiceNumber() {
        if (this.invoiceNumber == null) {
            // Format: INV-YYYYMMDD-STUDENTID-RANDOM
            String date = LocalDate.now().toString().replace("-", "");
            String studentId = String.valueOf(this.student.getId());
            String random = String.valueOf((int)(Math.random() * 10000));
            this.invoiceNumber = String.format("INV-%s-%s-%s", date, studentId, random);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return id != null && id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format(
            "Payment{id=%d, student=%s, amount=%s, type=%s, status=%s, dueDate=%s}",
            id,
            student != null ? student.getEmail() : "null",
            amount,
            paymentType,
            status,
            dueDate
        );
    }
}
