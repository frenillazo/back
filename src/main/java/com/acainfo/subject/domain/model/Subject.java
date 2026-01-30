package com.acainfo.subject.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Subject domain entity - Anemic model with Lombok.
 * Business logic (including validations) resides in application services.
 * Contains only query methods for domain-specific operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "code")
@ToString
public class Subject {

    private Long id;
    private String code;           // e.g., "ING101"
    private String name;           // e.g., "Programación I"
    private Degree degree;
    private Integer year;          // Academic year (1-4), nullable for legacy data
    private SubjectStatus status;

    @Builder.Default
    private Integer currentGroupCount = 0; // Number of groups associated

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== Query Methods Only (No Business Logic) ==========

    /**
     * Check if subject is active.
     */
    public boolean isActive() {
        return status == SubjectStatus.ACTIVE;
    }

    /**
     * Check if subject is archived.
     */
    public boolean isArchived() {
        return status == SubjectStatus.ARCHIVED;
    }

    /**
     * Check if a new group can be created for this subject.
     * A subject can have unlimited groups as long as it's active.
     */
    public boolean canCreateGroup() {
        return isActive();
    }

    /**
     * Get display name combining code and name (computed property).
     */
    public String getDisplayName() {
        return code + " - " + name;
    }
}
