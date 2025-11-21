package acainfo.back.material.domain.model;

import acainfo.back.shared.domain.model.User;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing educational material (files) for a subject group.
 *
 * Business rules:
 * - Only teachers can upload materials
 * - Only students with active enrollment can access materials
 * - Materials can require payment validation before access
 * - Supported file types: PDF, Java, C++, Header files
 * - Files are stored locally in the file system
 * - Materials can be organized by topic/unit
 * - Soft delete is used (isActive flag)
 * - Version field for optimistic locking
 */
@Entity
@Table(
    name = "materials",
    indexes = {
        @Index(name = "idx_material_group", columnList = "subject_group_id"),
        @Index(name = "idx_material_type", columnList = "type"),
        @Index(name = "idx_material_active", columnList = "is_active"),
        @Index(name = "idx_material_topic", columnList = "topic")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The subject group this material belongs to
     */
    @NotNull(message = "Subject group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroup subjectGroup;

    /**
     * Original file name (as uploaded)
     */
    @NotBlank(message = "File name is required")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * Path to the file in the storage system
     * Format: {groupId}/{uuid}_{filename}
     */
    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * Type of the file (determined from extension)
     */
    @NotNull(message = "File type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType type;

    /**
     * Size of the file in bytes
     */
    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * Optional description of the material
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    /**
     * Topic or unit this material belongs to (e.g., "Tema 1", "Unidad 3")
     */
    @Size(max = 100, message = "Topic must not exceed 100 characters")
    @Column(length = 100)
    private String topic;

    /**
     * User who uploaded the material (teacher)
     */
    @NotNull(message = "Uploader is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /**
     * When the material was uploaded
     */
    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Whether this material requires payment validation before access
     * If true, students with overdue payments cannot access this material
     */
    @NotNull(message = "Requires payment flag is required")
    @Column(name = "requires_payment", nullable = false)
    @Builder.Default
    private Boolean requiresPayment = true;

    /**
     * Whether this material is active (soft delete)
     * Inactive materials are not shown to students
     */
    @NotNull(message = "Active flag is required")
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Version field for optimistic locking
     * Incremented on each update to prevent concurrent modification issues
     */
    @Version
    @Column(nullable = false)
    private Integer version;

    // ==================== BUSINESS METHODS ====================

    /**
     * Deactivates the material (soft delete)
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Reactivates the material
     */
    public void activate() {
        this.isActive = true;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Material)) return false;
        Material material = (Material) o;
        return id != null && id.equals(material.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", type=" + type +
                ", size=" + getFormattedFileSize() +
                ", topic='" + topic + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
