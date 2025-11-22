package acainfo.back.material.infrastructure.adapters.in.dto;

import acainfo.back.material.domain.model.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for material responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Material information")
public class MaterialResponse {

    @Schema(description = "Material ID", example = "1")
    private Long id;

    @Schema(description = "Subject group ID", example = "10")
    private Long subjectGroupId;

    @Schema(description = "Subject group name", example = "Programación I - Grupo A")
    private String subjectGroupName;

    @Schema(description = "Original file name", example = "ejercicios_tema1.pdf")
    private String fileName;

    @Schema(description = "File type", example = "PDF")
    private MaterialType type;

    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSize;

    @Schema(description = "Formatted file size", example = "1.00 MB")
    private String formattedFileSize;

    @Schema(description = "Description of the material", example = "Ejercicios del tema 1")
    private String description;

    @Schema(description = "Topic or unit", example = "Tema 1")
    private String topic;

    @Schema(description = "Uploader user ID", example = "5")
    private Long uploadedById;

    @Schema(description = "Uploader full name", example = "Prof. Juan García")
    private String uploadedByName;

    @Schema(description = "Upload timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime uploadedAt;

    @Schema(description = "Requires payment validation", example = "true")
    private Boolean requiresPayment;

    @Schema(description = "Is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Integer version;
}
