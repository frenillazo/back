package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import com.acainfo.material.domain.model.MaterialCategory;
import java.time.LocalDateTime;

/**
 * REST response DTO for Material metadata.
 *
 * Enriched with related entity data to reduce frontend API calls.
 */
public record MaterialResponse(
        Long id,
        Long subjectId,
        Long uploadedById,
        String name,
        String description,
        String originalFilename,
        String fileExtension,
        String mimeType,
        Long fileSize,
        String fileSizeFormatted,
        boolean isCodeFile,
        boolean isDocumentFile,
        MaterialCategory category,
        String categoryDisplayName,
        LocalDateTime uploadedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        // Enriched data from related entities
        String subjectName,
        String uploadedByName
) {
}
