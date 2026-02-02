package com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity;

import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Enrollment persistence.
 * Maps to 'enrollments' table in database.
 */
@Entity
@Table(
    name = "enrollments",
    indexes = {
        @Index(name = "idx_enrollment_student_id", columnList = "student_id"),
        @Index(name = "idx_enrollment_group_id", columnList = "group_id"),
        @Index(name = "idx_enrollment_status", columnList = "status"),
        @Index(name = "idx_enrollment_student_group", columnList = "student_id, group_id"),
        @Index(name = "idx_enrollment_group_status", columnList = "group_id, status"),
        @Index(name = "idx_enrollment_waiting_list", columnList = "group_id, status, waiting_list_position")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_enrollment_student_group_active",
            columnNames = {"student_id", "group_id", "status"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "waiting_list_position")
    private Integer waitingListPosition;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
