package acainfo.back.domain.model;

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
 * Entity representing a group/class in the training center.
 * A group is a specific instance of a subject with assigned teacher and schedule.
 * Classrooms are now assigned per schedule slot, not per group.
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
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The subject this group belongs to
     */
    @NotNull(message = "Subject is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * The teacher assigned to this group
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    /**
     * Type of group (REGULAR or INTENSIVO)
     */
    @NotNull(message = "Group type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupType type;

    /**
     * Academic period when this group runs
     */
    @NotNull(message = "Academic period is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcademicPeriod period;

    /**
     * Current status of the group
     */
    @NotNull(message = "Group status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVO;

    /**
     * Maximum capacity for this group.
     * This represents the maximum enrollment limit for the entire group.
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
     * Optional description or notes about the group
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    /**
     * Weekly schedules for this group.
     * Defines when the group meets (day + time slots).
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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
     * Checks if the group is active
     */
    public boolean isActive() {
        return status == GroupStatus.ACTIVO;
    }

    /**
     * Checks if the group is full
     */
    public boolean isFull() {
        return currentOccupancy >= maxCapacity;
    }

    /**
     * Checks if the group has available places
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
     * Activates the group
     */
    public void activate() {
        if (!isFull()) {
            this.status = GroupStatus.ACTIVO;
        }
    }

    /**
     * Deactivates the group
     */
    public void deactivate() {
        this.status = GroupStatus.INACTIVO;
    }

    /**
     * Cancels the group
     */
    public void cancel() {
        this.status = GroupStatus.CANCELADO;
    }

    /**
     * Marks the group as full
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
     * Checks if the group has schedules defined
     */
    public boolean hasSchedules() {
        return schedules != null && !schedules.isEmpty();
    }

    /**
     * Adds a schedule to the group
     */
    public void addSchedule(Schedule schedule) {
        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        schedules.add(schedule);
        schedule.setGroup(this);
    }

    /**
     * Removes a schedule from the group
     */
    public void removeSchedule(Schedule schedule) {
        if (schedules != null) {
            schedules.remove(schedule);
            schedule.setGroup(null);
        }
    }

    /**
     * Gets the total hours per week for this group
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
     * Checks if this is a regular group
     */
    public boolean isRegular() {
        return type == GroupType.REGULAR;
    }

    /**
     * Checks if this is an intensive group
     */
    public boolean isIntensive() {
        return type == GroupType.INTENSIVO;
    }

    /**
     * Gets a descriptive name for the group
     */
    public String getDisplayName() {
        if (subject == null) return "Group #" + id;
        return subject.getCode() + " - " + type.getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return id != null && id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", subject=" + (subject != null ? subject.getCode() : "null") +
                ", type=" + type +
                ", period=" + period +
                ", status=" + status +
                ", occupancy=" + currentOccupancy + "/" + maxCapacity +
                '}';
    }
}
