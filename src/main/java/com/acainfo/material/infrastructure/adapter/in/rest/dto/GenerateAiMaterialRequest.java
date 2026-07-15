package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Metadata part of POST /api/materials/ai/generate (multipart with N images).
 *
 * @param subjectId     Subject the resulting material belongs to
 * @param folderId      Destination folder (null = subject root)
 * @param exerciseCount Exercises to generate (null = default 2)
 */
public record GenerateAiMaterialRequest(
        @NotNull(message = "El ID de asignatura es obligatorio")
        Long subjectId,

        Long folderId,

        @Min(value = 1, message = "Debe generarse al menos 1 ejercicio")
        @Max(value = 10, message = "Como máximo se generan 10 ejercicios")
        Integer exerciseCount
) {

    public int exerciseCountOrDefault() {
        return exerciseCount != null ? exerciseCount : 2;
    }
}
