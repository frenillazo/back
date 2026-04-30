package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/materials/{id}.
 * All fields are optional; null leaves the corresponding field unchanged.
 */
public record UpdateMaterialRequest(
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String name,

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String description,

        Boolean visible,

        Boolean downloadDisabled
) {
}
