package com.acainfo.material.domain.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Material domain entity - Anemic model with Lombok.
 * Represents educational material associated with a subject.
 *
 * <p>Business rules (enforced in application services):</p>
 * <ul>
 *   <li>Material is associated with a subject (shared across all groups)</li>
 *   <li>Only whitelisted file types are allowed</li>
 *   <li>All users can see material metadata</li>
 *   <li>Only students with active enrollments AND payments up to date can download</li>
 *   <li>Teachers and admins can always download</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Material {

    private Long id;

    /**
     * Reference to the subject this material belongs to.
     */
    private Long subjectId;

    /**
     * Reference to the user who uploaded the material (teacher or admin).
     */
    private Long uploadedById;

    /**
     * Display name of the material.
     */
    private String name;

    /**
     * Optional description of the material content.
     */
    private String description;

    /**
     * Category for organizing materials (e.g., TEORIA, EJERCICIOS).
     */
    @Builder.Default
    private MaterialCategory category = MaterialCategory.OTROS;

    /**
     * Original filename as uploaded.
     */
    private String originalFilename;

    /**
     * Stored filename (UUID-based to avoid collisions).
     */
    private String storedFilename;

    /**
     * File extension (e.g., "pdf", "java", "cpp").
     */
    private String fileExtension;

    /**
     * MIME type of the file.
     */
    private String mimeType;

    /**
     * File size in bytes.
     */
    private Long fileSize;

    /**
     * Storage path relative to base storage directory.
     */
    private String storagePath;

    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Query Methods ====================

    /**
     * Get file size in human-readable format.
     */
    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return (fileSize / 1024) + " KB";
        return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
    }

    /**
     * Check if this is a code file.
     */
    public boolean isCodeFile() {
        return fileExtension != null &&
                (fileExtension.equals("java") ||
                        fileExtension.equals("cpp") ||
                        fileExtension.equals("c") ||
                        fileExtension.equals("h") ||
                        fileExtension.equals("py"));
    }

    /**
     * Check if this is a document file.
     */
    public boolean isDocumentFile() {
        return fileExtension != null &&
                (fileExtension.equals("pdf") ||
                        fileExtension.equals("docx") ||
                        fileExtension.equals("txt") ||
                        fileExtension.equals("md"));
    }

    /**
     * Get category display name.
     */
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : MaterialCategory.OTROS.getDisplayName();
    }

    /**
     * Get category folder name for storage.
     */
    public String getCategoryFolderName() {
        return category != null ? category.getFolderName() : MaterialCategory.OTROS.getFolderName();
    }

    /**
     * Check if this material is in the default category.
     */
    public boolean isInDefaultCategory() {
        return category == null || category == MaterialCategory.OTROS;
    }
}
