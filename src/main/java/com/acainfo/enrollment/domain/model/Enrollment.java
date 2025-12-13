package com.acainfo.enrollment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Enrollment domain entity - Anemic model with Lombok.
 * Represents a student's enrollment in a subject group.
 *
 * <p>Business rules (enforced in application services):</p>
 * <ul>
 *   <li>Automatic promotion from waiting list when seats become available (FIFO)</li>
 *   <li>One active enrollment per student per group</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Enrollment {

    private Long id;

    /**
     * Reference to the enrolled student (User with STUDENT role).
     */
    private Long studentId;

    /**
     * Reference to the subject group the student is enrolled in.
     */
    private Long groupId;

    /**
     * Price per hour for this enrollment (€/hour).
     * Set at enrollment time, used for payment calculations.
     */
    private BigDecimal pricePerHour;

    /**
     * Current status of this enrollment.
     */
    private EnrollmentStatus status;

    /**
     * Position in the waiting list. Null if not in waiting list.
     * Used for FIFO promotion when seats become available.
     */
    private Integer waitingListPosition;

    /**
     * Date when the enrollment was created.
     */
    private LocalDateTime enrolledAt;

    /**
     * Date when the student was promoted from waiting list to active.
     * Null if enrolled directly or still in waiting list.
     */
    private LocalDateTime promotedAt;

    /**
     * Date when the student withdrew from the group.
     * Null if not withdrawn.
     */
    private LocalDateTime withdrawnAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Status Query Methods ====================

    /**
     * Check if student is actively enrolled.
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    /**
     * Check if student is on the waiting list.
     */
    public boolean isOnWaitingList() {
        return status == EnrollmentStatus.WAITING_LIST;
    }

    /**
     * Check if student has withdrawn.
     */
    public boolean isWithdrawn() {
        return status == EnrollmentStatus.WITHDRAWN;
    }

    /**
     * Check if student has completed the course.
     */
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }

    // ==================== Computed Properties ====================

    /**
     * Check if this enrollment was promoted from waiting list.
     */
    public boolean wasPromotedFromWaitingList() {
        return promotedAt != null;
    }

    /**
     * Check if enrollment can be withdrawn (active or on waiting list).
     */
    public boolean canBeWithdrawn() {
        return isActive() || isOnWaitingList();
    }
}
