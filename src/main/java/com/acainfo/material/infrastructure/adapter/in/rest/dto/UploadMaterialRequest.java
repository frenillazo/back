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
        @NotNull(message = "Subject ID is required")
        Long subjectId,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
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
