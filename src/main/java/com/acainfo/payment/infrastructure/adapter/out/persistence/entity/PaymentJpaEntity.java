package com.acainfo.payment.infrastructure.adapter.out.persistence.entity;

import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Payment persistence.
 * Maps to 'payments' table in database.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_student_id", columnList = "student_id"),
        @Index(name = "idx_payment_enrollment_id", columnList = "enrollment_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_type", columnList = "type"),
        @Index(name = "idx_payment_due_date", columnList = "due_date"),
        @Index(name = "idx_payment_billing_period", columnList = "billing_year, billing_month"),
        @Index(name = "idx_payment_student_status", columnList = "student_id, status"),
        @Index(name = "idx_payment_overdue", columnList = "status, due_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payment_enrollment_period",
            columnNames = {"enrollment_id", "billing_month", "billing_year", "type"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "total_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "price_per_hour", nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "billing_month")
    private Integer billingMonth;

    @Column(name = "billing_year")
    private Integer billingYear;

    @Column(name = "generated_at", nullable = false)
    private LocalDate generatedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
