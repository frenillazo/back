package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

/**
 * REST response DTO for Material metadata.
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
        LocalDateTime uploadedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
