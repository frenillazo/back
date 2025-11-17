package acainfo.back.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Schedule entity representing a weekly time slot for a group.
 * Each schedule defines when a group meets (day + time range).
 */
@Entity
@Table(name = "schedules", indexes = {
        @Index(name = "idx_schedule_group", columnList = "group_id"),
        @Index(name = "idx_schedule_day", columnList = "day_of_week"),
        @Index(name = "idx_schedule_time", columnList = "start_time, end_time")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @NotNull(message = "Group is required")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime updatedAt;

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Validates that start time is before end time.
     *
     * @throws IllegalArgumentException if start time is not before end time
     */
    @PrePersist
    @PreUpdate
    private void validateTimes() {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time. Start: " + startTime + ", End: " + endTime
            );
        }
    }

    /**
     * Calculates the duration of this schedule in minutes.
     *
     * @return duration in minutes
     */
    public long getDurationInMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Calculates the duration of this schedule.
     *
     * @return Duration object
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Checks if this schedule is on the same day as another schedule.
     *
     * @param other the other schedule
     * @return true if same day, false otherwise
     */
    public boolean isOnSameDay(Schedule other) {
        return this.dayOfWeek == other.dayOfWeek;
    }

    /**
     * Checks if this schedule overlaps with another schedule.
     * Two schedules overlap if they are on the same day and their time ranges intersect.
     *
     * @param other the other schedule
     * @return true if schedules overlap, false otherwise
     */
    public boolean overlaps(Schedule other) {
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
    public boolean conflictsWith(Schedule other) {
        return overlaps(other);
    }

    /**
     * Gets a human-readable representation of the schedule.
     * Example: "MONDAY 10:00-12:00"
     *
     * @return formatted schedule string
     */
    public String getFormattedSchedule() {
        return String.format("%s %s-%s",
                dayOfWeek.name(),
                startTime.toString(),
                endTime.toString()
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
     * Example: "Lunes 10:00-12:00"
     *
     * @return formatted schedule string in Spanish
     */
    public String getFormattedScheduleSpanish() {
        return String.format("%s %s-%s",
                getLocalizedDayName(),
                startTime.toString(),
                endTime.toString()
        );
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
