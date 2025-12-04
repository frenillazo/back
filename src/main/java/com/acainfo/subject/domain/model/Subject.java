package com.acainfo.subject.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing an academic subject.
 * This is a pure POJO with no framework dependencies (Anemic Domain Model).
 * Business logic is minimal - only query methods and simple validations.
 */
public class Subject {

    private Long id;
    private String code;           // e.g., "ING101"
    private String name;           // e.g., "Programación I"
    private String description;
    private Integer credits;       // e.g., 6 créditos
    private Degree degree;
    private SubjectStatus status;
    private Integer currentGroupCount; // Number of groups associated
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors

    public Subject() {
        this.status = SubjectStatus.ACTIVE;
        this.currentGroupCount = 0;
    }

    public Subject(Long id, String code, String name, String description,
                   Integer credits, Degree degree, SubjectStatus status,
                   Integer currentGroupCount, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.degree = degree;
        this.status = status != null ? status : SubjectStatus.ACTIVE;
        this.currentGroupCount = currentGroupCount != null ? currentGroupCount : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Query methods (domain logic)

    /**
     * Checks if the subject is active.
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return this.status == SubjectStatus.ACTIVE;
    }

    /**
     * Checks if the subject is archived.
     * @return true if status is ARCHIVED
     */
    public boolean isArchived() {
        return this.status == SubjectStatus.ARCHIVED;
    }

    /**
     * Checks if a new group can be created for this subject.
     * Based on the business rule: maximum 3 groups per subject.
     * @return true if another group can be created
     */
    public boolean canCreateGroup() {
        return isActive() && currentGroupCount < 3;
    }

    /**
     * Gets the number of remaining group slots available.
     * @return remaining slots (0-3)
     */
    public int getRemainingGroupSlots() {
        return Math.max(0, 3 - currentGroupCount);
    }

    /**
     * Gets the display name combining code and name.
     * @return formatted display name
     */
    public String getDisplayName() {
        return code + " - " + name;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Degree getDegree() {
        return degree;
    }

    public void setDegree(Degree degree) {
        this.degree = degree;
    }

    public SubjectStatus getStatus() {
        return status;
    }

    public void setStatus(SubjectStatus status) {
        this.status = status;
    }

    public Integer getCurrentGroupCount() {
        return currentGroupCount;
    }

    public void setCurrentGroupCount(Integer currentGroupCount) {
        this.currentGroupCount = currentGroupCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals and hashCode based on business key (code)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(code, subject.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", credits=" + credits +
                ", degree=" + degree +
                ", status=" + status +
                ", currentGroupCount=" + currentGroupCount +
                '}';
    }
}
