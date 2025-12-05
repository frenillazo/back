package com.acainfo.schedule.domain.model;

import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Schedule domain entity (POJO).
 * Represents a schedule for a group on a specific day and time.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Schedule {

    private Long id;
    private Long groupId;           // Reference to SubjectGroup (aggregate independence)
    private DayOfWeek dayOfWeek;    // MONDAY, TUESDAY, etc.
    private LocalTime startTime;    // 09:00
    private LocalTime endTime;      // 11:00
    private Classroom classroom;    // Enum: AULA_PORTAL1, AULA_PORTAL2, AULA_VIRTUAL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Query methods

    /**
     * Check if this schedule conflicts with another schedule.
     * Conflict occurs when:
     * - Same day of week
     * - Time overlaps
     * - Same classroom (classroom conflict)
     */
    public boolean conflictsWith(Schedule other) {
        if (other == null || this.id != null && this.id.equals(other.getId())) {
            return false; // Don't check conflict with itself
        }

        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false; // Different days, no conflict
        }

        return timeOverlaps(other) && classroomConflicts(other);
    }

    /**
     * Check if time ranges overlap.
     */
    private boolean timeOverlaps(Schedule other) {
        // Times overlap if: start1 < end2 AND end1 > start2
        return this.startTime.isBefore(other.endTime) &&
               this.endTime.isAfter(other.startTime);
    }

    /**
     * Check if classrooms conflict (same classroom).
     */
    private boolean classroomConflicts(Schedule other) {
        if (this.classroom == null || other.classroom == null) {
            return false; // No conflict if classroom not assigned
        }
        return this.classroom == other.classroom;
    }

    /**
     * Get duration of the schedule.
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Get duration in minutes.
     */
    public long getDurationMinutes() {
        return getDuration().toMinutes();
    }

    /**
     * Check if schedule is valid (start before end).
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
