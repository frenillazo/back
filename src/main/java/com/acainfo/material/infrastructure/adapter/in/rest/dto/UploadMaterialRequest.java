package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import com.acainfo.material.domain.model.MaterialCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST request DTO for uploading material metadata.
 * File content is handled separately via MultipartFile.
 */
public record UploadMaterialRequest(
        @NotNull(message = "El ID de asignatura es obligatorio")
        Long subjectId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String name,

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String description,

        MaterialCategory category
) {
    /**
     * Returns the category or OTROS if not provided.
     */
    public MaterialCategory getCategoryOrDefault() {
        return category != null ? category : MaterialCategory.OTROS;
    }
}
