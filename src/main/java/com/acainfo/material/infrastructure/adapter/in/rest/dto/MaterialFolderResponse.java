package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

/**
 * REST response DTO for a material folder.
 */
public record MaterialFolderResponse(
        Long id,
        Long subjectId,
        String name,
        int position,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
