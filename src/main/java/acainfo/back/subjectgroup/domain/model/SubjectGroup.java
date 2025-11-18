package acainfo.back.subjectgroup.domain.model;

import acainfo.back.shared.domain.model.User;
import acainfo.back.schedule.domain.model.Schedule;
import acainfo.back.subject.domain.model.Subject;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a subjectGroup/class in the training center.
 * A subjectGroup is a specific instance of a subject with assigned teacher and schedule.
 * Classrooms are now assigned per schedule slot, not per subjectGroup.
 */
@Entity
@Table(
    name = "groups",
    indexes = {
        @Index(name = "idx_group_subject", columnList = "subject_id"),
        @Index(name = "idx_group_teacher", columnList = "teacher_id"),
        @Index(name = "idx_group_status", columnList = "status"),
        @Index(name = "idx_group_type", columnList = "type"),
        @Index(name = "idx_group_period", columnList = "period")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The subject this subjectGroup belongs to
     */
    @NotNull(message = "Subject is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * The teacher assigned to this subjectGroup
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    /**
     * Type of subjectGroup (REGULAR or INTENSIVO)
     */
    @NotNull(message = "SubjectGroup type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupType type;

    /**
     * Academic period when this subjectGroup runs
     */
    @NotNull(message = "Academic period is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcademicPeriod period;

    /**
     * Current status of the subjectGroup
     */
    @NotNull(message = "SubjectGroup status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVO;

    /**
     * Maximum capacity for this subjectGroup.
     * This represents the maximum enrollment limit for the entire subjectGroup.
     */
    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    /**
     * Current number of enrolled students
     */
    @NotNull(message = "Current occupancy is required")
    @Min(value = 0, message = "Current occupancy cannot be negative")
    @Column(name = "current_occupancy", nullable = false)
    @Builder.Default
    private Integer currentOccupancy = 0;

    /**
     * Optional description or notes about the subjectGroup
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    /**
     * Weekly schedules for this subjectGroup.
     * Defines when the subjectGroup meets (day + time slots).
     */
    @OneToMany(mappedBy = "subjectGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== HELPER METHODS ====================

    /**
     * Checks if the subjectGroup is active
     */
    public boolean isActive() {
        return status == GroupStatus.ACTIVO;
    }

    /**
     * Checks if the subjectGroup is full
     */
    public boolean isFull() {
        return currentOccupancy >= maxCapacity;
    }

    /**
     * Checks if the subjectGroup has available places
     */
    public boolean hasAvailablePlaces() {
        return currentOccupancy < maxCapacity && status == GroupStatus.ACTIVO;
    }

    /**
     * Gets the number of available places
     */
    public int getAvailablePlaces() {
        return Math.max(0, maxCapacity - currentOccupancy);
    }

    /**
     * Gets the occupancy percentage
     */
    public double getOccupancyPercentage() {
        if (maxCapacity == 0) return 0.0;
        return (currentOccupancy * 100.0) / maxCapacity;
    }

    /**
     * Increments the current occupancy
     */
    public void incrementOccupancy() {
        if (currentOccupancy < maxCapacity) {
            currentOccupancy++;
            if (currentOccupancy >= maxCapacity) {
                status = GroupStatus.COMPLETO;
            }
        }
    }

    /**
     * Decrements the current occupancy
     */
    public void decrementOccupancy() {
        if (currentOccupancy > 0) {
            currentOccupancy--;
            if (status == GroupStatus.COMPLETO && currentOccupancy < maxCapacity) {
                status = GroupStatus.ACTIVO;
            }
        }
    }

    /**
     * Activates the subjectGroup
     */
    public void activate() {
        if (!isFull()) {
            this.status = GroupStatus.ACTIVO;
        }
    }

    /**
     * Deactivates the subjectGroup
     */
    public void deactivate() {
        this.status = GroupStatus.INACTIVO;
    }

    /**
     * Cancels the subjectGroup
     */
    public void cancel() {
        this.status = GroupStatus.CANCELADO;
    }

    /**
     * Marks the subjectGroup as full
     */
    public void markAsFull() {
        this.status = GroupStatus.COMPLETO;
    }

    /**
     * Checks if teacher is assigned
     */
    public boolean hasTeacher() {
        return teacher != null;
    }

    /**
     * Checks if the subjectGroup has schedules defined
     */
    public boolean hasSchedules() {
        return schedules != null && !schedules.isEmpty();
    }

    /**
     * Adds a schedule to the subjectGroup
     */
    public void addSchedule(Schedule schedule) {
        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        schedules.add(schedule);
        schedule.setSubjectGroup(this);
    }

    /**
     * Removes a schedule from the subjectGroup
     */
    public void removeSchedule(Schedule schedule) {
        if (schedules != null) {
            schedules.remove(schedule);
            schedule.setSubjectGroup(null);
        }
    }

    /**
     * Gets the total hours per week for this subjectGroup
     */
    public long getTotalWeeklyHours() {
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }
        return schedules.stream()
                .mapToLong(Schedule::getDurationInMinutes)
                .sum() / 60;
    }

    /**
     * Checks if this is a regular subjectGroup
     */
    public boolean isRegular() {
        return type == GroupType.REGULAR;
    }

    /**
     * Checks if this is an intensive subjectGroup
     */
    public boolean isIntensive() {
        return type == GroupType.INTENSIVO;
    }

    /**
     * Gets a descriptive name for the subjectGroup
     */
    public String getDisplayName() {
        if (subject == null) return "SubjectGroup #" + id;
        return subject.getCode() + " - " + type.getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectGroup)) return false;
        SubjectGroup subjectGroup = (SubjectGroup) o;
        return id != null && id.equals(subjectGroup.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SubjectGroup{" +
                "id=" + id +
                ", subject=" + (subject != null ? subject.getCode() : "null") +
                ", type=" + type +
                ", period=" + period +
                ", status=" + status +
                ", occupancy=" + currentOccupancy + "/" + maxCapacity +
                '}';
    }
}
