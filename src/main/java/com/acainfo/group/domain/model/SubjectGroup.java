package com.acainfo.group.domain.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * SubjectGroup domain entity (anemic model with Lombok).
 * Represents a group of students taking a subject with a specific teacher.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class SubjectGroup {

    // Capacity constants
    public static final int REGULAR_MAX_CAPACITY = 24;
    public static final int INTENSIVE_MAX_CAPACITY = 50;

    private Long id;
    private Long subjectId;                      // Reference to Subject
    private Long teacherId;                      // Reference to User (teacher)
    private GroupType type;                      // REGULAR_Q1, INTENSIVE_Q1, etc.
    private GroupStatus status;                  // OPEN, CLOSED, CANCELLED

    @Builder.Default
    private Integer currentEnrollmentCount = 0;  // Current enrolled students

    private Integer capacity;                    // Custom capacity (optional, null = use default)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Query Methods ====================

    /**
     * Check if group is open for enrollments.
     */
    public boolean isOpen() {
        return status == GroupStatus.OPEN;
    }

    /**
     * Check if group is closed.
     */
    public boolean isClosed() {
        return status == GroupStatus.CLOSED;
    }

    /**
     * Check if group is cancelled.
     */
    public boolean isCancelled() {
        return status == GroupStatus.CANCELLED;
    }

    /**
     * Check if this is a regular group (Q1 or Q2).
     */
    public boolean isRegular() {
        return type != null && type.isRegular();
    }

    /**
     * Check if this is an intensive group (Q1 or Q2).
     */
    public boolean isIntensive() {
        return type != null && type.isIntensive();
    }

    /**
     * Get the maximum capacity for this group based on its type.
     * Regular: 24 students (mostly in-person sessions)
     * Intensive: 50 students (dual/hybrid sessions)
     * If custom capacity is set, returns the custom value.
     *
     * @return Maximum capacity
     */
    public int getMaxCapacity() {
        // If custom capacity is set, use it
        if (capacity != null) {
            return capacity;
        }

        // Otherwise, use default based on type
        return isIntensive() ? INTENSIVE_MAX_CAPACITY : REGULAR_MAX_CAPACITY;
    }

    /**
     * Get the number of available seats remaining.
     *
     * @return Available seats (never negative)
     */
    public int getAvailableSeats() {
        return Math.max(0, getMaxCapacity() - currentEnrollmentCount);
    }

    /**
     * Check if the group has available seats.
     *
     * @return true if there are available seats
     */
    public boolean hasAvailableSeats() {
        return getAvailableSeats() > 0;
    }

    /**
     * Check if the group is full (no more seats available).
     *
     * @return true if the group is at maximum capacity
     */
    public boolean isFull() {
        return currentEnrollmentCount >= getMaxCapacity();
    }

    /**
     * Check if a student can enroll in this group.
     * Requires: group is open AND has available seats.
     *
     * @return true if enrollment is possible
     */
    public boolean canEnroll() {
        return isOpen() && hasAvailableSeats();
    }

    /**
     * Get display name combining subject and group type.
     * Format: "Subject [ID] - TYPE"
     *
     * @return Display name
     */
    public String getDisplayName() {
        return "Subject " + subjectId + " - " + type;
    }
}
