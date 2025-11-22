package acainfo.back.subjectgroup.domain.model;

import acainfo.back.schedule.domain.model.ScheduleDomain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain Model for SubjectGroup
 * Pure POJO - NO infrastructure dependencies (NO JPA annotations)
 *
 * Represents a specific instance of a subject with assigned teacher and schedule.
 *
 * Business Invariants:
 * - Subject is required
 * - Type and period are required
 * - Max capacity must be at least 1
 * - Current occupancy cannot be negative
 * - Current occupancy cannot exceed max capacity
 */
public class SubjectGroupDomain {

    private Long id;
    private Long subjectId; // Reference to Subject (not entity reference)
    private Long teacherId; // Reference to User (teacher, can be null)
    private GroupType type;
    private AcademicPeriod period;
    private GroupStatus status;
    private Integer maxCapacity;
    private Integer currentOccupancy;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor - force use of builder
    private SubjectGroupDomain() {
    }

    // ==================== FACTORY METHOD ====================

    public static SubjectGroupDomainBuilder builder() {
        return new SubjectGroupDomainBuilder();
    }

    // ==================== GETTERS ====================

    public Long getId() {
        return id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public GroupType getType() {
        return type;
    }

    public AcademicPeriod getPeriod() {
        return period;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

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
     * Increments the current occupancy and updates status if needed
     * Returns a new instance with updated values (immutable-style)
     */
    public SubjectGroupDomain incrementOccupancy() {
        if (currentOccupancy >= maxCapacity) {
            throw new IllegalStateException("Cannot increment occupancy: group is already full");
        }

        int newOccupancy = currentOccupancy + 1;
        GroupStatus newStatus = (newOccupancy >= maxCapacity) ? GroupStatus.COMPLETO : this.status;

        return SubjectGroupDomain.builder()
                .id(this.id)
                .subjectId(this.subjectId)
                .teacherId(this.teacherId)
                .type(this.type)
                .period(this.period)
                .status(newStatus)
                .maxCapacity(this.maxCapacity)
                .currentOccupancy(newOccupancy)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Decrements the current occupancy and updates status if needed
     * Returns a new instance with updated values (immutable-style)
     */
    public SubjectGroupDomain decrementOccupancy() {
        if (currentOccupancy <= 0) {
            throw new IllegalStateException("Cannot decrement occupancy: already at 0");
        }

        int newOccupancy = currentOccupancy - 1;
        GroupStatus newStatus = this.status;

        // If was full and now has space, reactivate
        if (this.status == GroupStatus.COMPLETO && newOccupancy < maxCapacity) {
            newStatus = GroupStatus.ACTIVO;
        }

        return SubjectGroupDomain.builder()
                .id(this.id)
                .subjectId(this.subjectId)
                .teacherId(this.teacherId)
                .type(this.type)
                .period(this.period)
                .status(newStatus)
                .maxCapacity(this.maxCapacity)
                .currentOccupancy(newOccupancy)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Activates the subjectGroup (if not full)
     */
    public void activate() {
        if (isFull()) {
            throw new IllegalStateException("Cannot activate: group is full");
        }
        this.status = GroupStatus.ACTIVO;
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
        return teacherId != null;
    }

    /**
     * Gets the total hours per week for this subjectGroup
     * Requires schedules to be passed in (domain doesn't maintain collections)
     */
    public long getTotalWeeklyHours(List<ScheduleDomain> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }
        return schedules.stream()
                .mapToLong(ScheduleDomain::getDurationInMinutes)
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
     * Requires subject code to be passed in
     */
    public String getDisplayName(String subjectCode) {
        if (subjectCode == null || subjectCode.isBlank()) {
            return "SubjectGroup #" + id;
        }
        return subjectCode + " - " + type.getDisplayName();
    }

    @Override
    public String toString() {
        return "SubjectGroupDomain{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", teacherId=" + teacherId +
                ", type=" + type +
                ", period=" + period +
                ", status=" + status +
                ", occupancy=" + currentOccupancy + "/" + maxCapacity +
                '}';
    }

    // ==================== BUILDER ====================

    public static class SubjectGroupDomainBuilder {
        private final SubjectGroupDomain group = new SubjectGroupDomain();

        public SubjectGroupDomainBuilder id(Long id) {
            group.id = id;
            return this;
        }

        public SubjectGroupDomainBuilder subjectId(Long subjectId) {
            group.subjectId = subjectId;
            return this;
        }

        public SubjectGroupDomainBuilder teacherId(Long teacherId) {
            group.teacherId = teacherId;
            return this;
        }

        public SubjectGroupDomainBuilder type(GroupType type) {
            group.type = type;
            return this;
        }

        public SubjectGroupDomainBuilder period(AcademicPeriod period) {
            group.period = period;
            return this;
        }

        public SubjectGroupDomainBuilder status(GroupStatus status) {
            group.status = status;
            return this;
        }

        public SubjectGroupDomainBuilder maxCapacity(Integer maxCapacity) {
            group.maxCapacity = maxCapacity;
            return this;
        }

        public SubjectGroupDomainBuilder currentOccupancy(Integer currentOccupancy) {
            group.currentOccupancy = currentOccupancy;
            return this;
        }

        public SubjectGroupDomainBuilder description(String description) {
            group.description = description;
            return this;
        }

        public SubjectGroupDomainBuilder createdAt(LocalDateTime createdAt) {
            group.createdAt = createdAt;
            return this;
        }

        public SubjectGroupDomainBuilder updatedAt(LocalDateTime updatedAt) {
            group.updatedAt = updatedAt;
            return this;
        }

        public SubjectGroupDomain build() {
            validate();
            return group;
        }

        private void validate() {
            // Required fields validation
            if (group.subjectId == null) {
                throw new IllegalArgumentException("Subject ID is required");
            }

            if (group.type == null) {
                throw new IllegalArgumentException("Group type is required");
            }

            if (group.period == null) {
                throw new IllegalArgumentException("Academic period is required");
            }

            if (group.status == null) {
                throw new IllegalArgumentException("Group status is required");
            }

            if (group.maxCapacity == null) {
                throw new IllegalArgumentException("Max capacity is required");
            }

            if (group.maxCapacity < 1) {
                throw new IllegalArgumentException("Max capacity must be at least 1");
            }

            if (group.currentOccupancy == null) {
                throw new IllegalArgumentException("Current occupancy is required");
            }

            if (group.currentOccupancy < 0) {
                throw new IllegalArgumentException("Current occupancy cannot be negative");
            }

            if (group.currentOccupancy > group.maxCapacity) {
                throw new IllegalArgumentException(
                        "Current occupancy (" + group.currentOccupancy +
                        ") cannot exceed max capacity (" + group.maxCapacity + ")"
                );
            }

            if (group.description != null && group.description.length() > 500) {
                throw new IllegalArgumentException("Description must not exceed 500 characters");
            }
        }
    }
}
