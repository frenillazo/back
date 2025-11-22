package acainfo.back.material.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Pure Domain Model for Material
 *
 * Represents educational material (files) for a subject group.
 *
 * Business Rules:
 * - Only teachers can upload materials
 * - Only students with active enrollment can access materials
 * - Materials can require payment validation before access
 * - Supported file types: PDF, Java, C++, Header files
 * - Files are stored locally in the file system
 * - Materials can be organized by topic/unit
 * - Soft delete is used (isActive flag)
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class MaterialDomain {

    private final Long id;

    /**
     * ID of the subject group this material belongs to
     */
    private final Long subjectGroupId;

    /**
     * Original file name (as uploaded)
     */
    private final String fileName;

    /**
     * Path to the file in the storage system
     * Format: {groupId}/{uuid}_{filename}
     */
    private final String filePath;

    /**
     * Type of the file (determined from extension)
     */
    private final MaterialType type;

    /**
     * Size of the file in bytes
     */
    private final Long fileSize;

    /**
     * Optional description of the material
     */
    private final String description;

    /**
     * Topic or unit this material belongs to (e.g., "Tema 1", "Unidad 3")
     */
    private final String topic;

    /**
     * ID of the user who uploaded the material (teacher)
     */
    private final Long uploadedById;

    /**
     * When the material was uploaded
     */
    private final LocalDateTime uploadedAt;

    /**
     * Whether this material requires payment validation before access
     * If true, students with overdue payments cannot access this material
     */
    private final Boolean requiresPayment;

    /**
     * Whether this material is active (soft delete)
     * Inactive materials are not shown to students
     */
    private final Boolean isActive;

    /**
     * Version field for optimistic locking
     */
    private final Integer version;

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the material data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (subjectGroupId == null) {
            throw new IllegalArgumentException("Subject group ID is required");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("File type is required");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("Valid file size is required");
        }
        if (uploadedById == null) {
            throw new IllegalArgumentException("Uploader ID is required");
        }
        if (requiresPayment == null) {
            throw new IllegalArgumentException("Requires payment flag is required");
        }
        if (isActive == null) {
            throw new IllegalArgumentException("Active flag is required");
        }

        // Validate description length
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("Description must not exceed 1000 characters");
        }

        // Validate topic length
        if (topic != null && topic.length() > 100) {
            throw new IllegalArgumentException("Topic must not exceed 100 characters");
        }
    }

    // ==================== STATE TRANSITION METHODS ====================

    /**
     * Deactivates the material (soft delete)
     * Returns a new MaterialDomain with isActive set to false
     */
    public MaterialDomain deactivate() {
        return this.toBuilder()
                .isActive(false)
                .build();
    }

    /**
     * Reactivates the material
     * Returns a new MaterialDomain with isActive set to true
     */
    public MaterialDomain activate() {
        return this.toBuilder()
                .isActive(true)
                .build();
    }

    /**
     * Updates the description
     * Returns a new MaterialDomain with updated description
     */
    public MaterialDomain updateDescription(String newDescription) {
        if (newDescription != null && newDescription.length() > 1000) {
            throw new IllegalArgumentException("Description must not exceed 1000 characters");
        }

        return this.toBuilder()
                .description(newDescription)
                .build();
    }

    /**
     * Updates the topic
     * Returns a new MaterialDomain with updated topic
     */
    public MaterialDomain updateTopic(String newTopic) {
        if (newTopic != null && newTopic.length() > 100) {
            throw new IllegalArgumentException("Topic must not exceed 100 characters");
        }

        return this.toBuilder()
                .topic(newTopic)
                .build();
    }

    /**
     * Updates whether payment is required
     * Returns a new MaterialDomain with updated requiresPayment flag
     */
    public MaterialDomain updateRequiresPayment(boolean requiresPayment) {
        return this.toBuilder()
                .requiresPayment(requiresPayment)
                .build();
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if material is a PDF document
     */
    public boolean isPdf() {
        return this.type == MaterialType.PDF;
    }

    /**
     * Check if material is a code file
     */
    public boolean isCode() {
        return this.type != null && this.type.isCode();
    }

    /**
     * Get file size in MB
     */
    public double getFileSizeInMB() {
        return fileSize / (1024.0 * 1024.0);
    }

    /**
     * Get upload date as Instant
     */
    public Instant getUploadDate() {
        return uploadedAt != null ? uploadedAt.atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
    }

    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "MaterialDomain{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", type=" + type +
                ", size=" + getFormattedFileSize() +
                ", topic='" + topic + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
