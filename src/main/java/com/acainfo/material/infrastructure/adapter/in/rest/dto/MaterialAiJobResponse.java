package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.domain.model.MaterialAiJobType;

import java.time.LocalDateTime;

/**
 * REST response DTO for an AI LaTeX job (the frontend polls it).
 */
public record MaterialAiJobResponse(
        Long id,
        MaterialAiJobType type,
        Long subjectId,
        Long sourceMaterialId,
        MaterialAiJobStatus status,
        String errorMessage,
        Long resultMaterialId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
