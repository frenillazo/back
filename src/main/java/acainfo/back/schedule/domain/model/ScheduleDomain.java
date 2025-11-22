package acainfo.back.schedule.domain.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Domain Model for Schedule
 * Pure POJO - NO infrastructure dependencies (NO JPA annotations)
 *
 * Represents a weekly time slot for a subject group.
 * Each schedule defines when a subject group meets (day + time range).
 *
 * Business Invariants:
 * - Start time must be before end time
 * - Day of week is required
 * - Classroom is required
 * - Subject group reference is required
 */
public class ScheduleDomain {

    private Long id;
    private Long subjectGroupId; // Reference to SubjectGroup (not entity reference)
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Classroom classroom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor - force use of builder
    private ScheduleDomain() {
    }

    // ==================== FACTORY METHOD ====================

    public static ScheduleDomainBuilder builder() {
        return new ScheduleDomainBuilder();
    }

    // ==================== GETTERS ====================

    public Long getId() {
        return id;
    }

    public Long getSubjectGroupId() {
        return subjectGroupId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Calculates the duration of this schedule in minutes.
     *
     * @return duration in minutes
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Calculates the duration of this schedule.
     *
     * @return Duration object
     */
    public Duration getDuration() {
        if (startTime == null || endTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(startTime, endTime);
    }

    /**
     * Checks if this schedule is on the same day as another schedule.
     *
     * @param other the other schedule
     * @return true if same day, false otherwise
     */
    public boolean isOnSameDay(ScheduleDomain other) {
        if (other == null) {
            return false;
        }
        return this.dayOfWeek == other.dayOfWeek;
    }

    /**
     * Checks if this schedule overlaps with another schedule.
     * Two schedules overlap if they are on the same day and their time ranges intersect.
     *
     * @param other the other schedule
     * @return true if schedules overlap, false otherwise
     */
    public boolean overlaps(ScheduleDomain other) {
        if (other == null) {
            return false;
        }

        // Must be on the same day
        if (!isOnSameDay(other)) {
            return false;
        }

        // Check time overlap
        // Two time ranges [s1, e1] and [s2, e2] overlap if:
        // s1 < e2 AND s2 < e1
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    /**
     * Checks if this schedule conflicts with another schedule for the same resource.
     * Used for detecting teacher or classroom conflicts.
     *
     * @param other the other schedule
     * @return true if there's a conflict, false otherwise
     */
    public boolean conflictsWith(ScheduleDomain other) {
        return overlaps(other);
    }

    /**
     * Gets a human-readable representation of the schedule.
     * Example: "MONDAY 10:00-12:00 (AULA_1)"
     *
     * @return formatted schedule string
     */
    public String getFormattedSchedule() {
        return String.format("%s %s-%s (%s)",
                dayOfWeek.name(),
                startTime.toString(),
                endTime.toString(),
                classroom.name()
        );
    }

    /**
     * Gets a localized day name.
     * Example: "Lunes" for MONDAY in Spanish
     *
     * @return localized day name
     */
    public String getLocalizedDayName() {
        return switch (dayOfWeek) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    /**
     * Gets a user-friendly schedule string in Spanish.
     * Example: "Lunes 10:00-12:00 (Aula 1)"
     *
     * @return formatted schedule string in Spanish
     */
    public String getFormattedScheduleSpanish() {
        return String.format("%s %s-%s (%s)",
                getLocalizedDayName(),
                startTime.toString(),
                endTime.toString(),
                classroom.getDisplayName()
        );
    }

    /**
     * Checks if this is a physical classroom schedule
     */
    public boolean isPhysicalClassroom() {
        return classroom != null && classroom.isPhysical();
    }

    /**
     * Checks if this is a virtual classroom schedule
     */
    public boolean isVirtualClassroom() {
        return classroom != null && classroom.isVirtual();
    }

    @Override
    public String toString() {
        return "ScheduleDomain{" +
                "id=" + id +
                ", subjectGroupId=" + subjectGroupId +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", classroom=" + classroom +
                '}';
    }

    // ==================== BUILDER ====================

    public static class ScheduleDomainBuilder {
        private final ScheduleDomain schedule = new ScheduleDomain();

        public ScheduleDomainBuilder id(Long id) {
            schedule.id = id;
            return this;
        }

        public ScheduleDomainBuilder subjectGroupId(Long subjectGroupId) {
            schedule.subjectGroupId = subjectGroupId;
            return this;
        }

        public ScheduleDomainBuilder dayOfWeek(DayOfWeek dayOfWeek) {
            schedule.dayOfWeek = dayOfWeek;
            return this;
        }

        public ScheduleDomainBuilder startTime(LocalTime startTime) {
            schedule.startTime = startTime;
            return this;
        }

        public ScheduleDomainBuilder endTime(LocalTime endTime) {
            schedule.endTime = endTime;
            return this;
        }

        public ScheduleDomainBuilder classroom(Classroom classroom) {
            schedule.classroom = classroom;
            return this;
        }

        public ScheduleDomainBuilder createdAt(LocalDateTime createdAt) {
            schedule.createdAt = createdAt;
            return this;
        }

        public ScheduleDomainBuilder updatedAt(LocalDateTime updatedAt) {
            schedule.updatedAt = updatedAt;
            return this;
        }

        public ScheduleDomain build() {
            validate();
            return schedule;
        }

        private void validate() {
            // Required fields validation
            if (schedule.subjectGroupId == null) {
                throw new IllegalArgumentException("Subject group ID is required");
            }

            if (schedule.dayOfWeek == null) {
                throw new IllegalArgumentException("Day of week is required");
            }

            if (schedule.startTime == null) {
                throw new IllegalArgumentException("Start time is required");
            }

            if (schedule.endTime == null) {
                throw new IllegalArgumentException("End time is required");
            }

            if (schedule.classroom == null) {
                throw new IllegalArgumentException("Classroom is required");
            }

            // Business rule: Start time must be before end time
            if (!schedule.startTime.isBefore(schedule.endTime)) {
                throw new IllegalArgumentException(
                        "Start time must be before end time. Start: " + schedule.startTime +
                        ", End: " + schedule.endTime
                );
            }
        }
    }
}
