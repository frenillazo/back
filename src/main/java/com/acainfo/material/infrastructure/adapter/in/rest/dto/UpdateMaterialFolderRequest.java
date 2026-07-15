package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/material-folders/{id}.
 * All fields are optional; null leaves the corresponding field unchanged.
 */
public record UpdateMaterialFolderRequest(
        @Size(max = 100, message = "El nombre de la carpeta no puede exceder 100 caracteres")
        String name,

        Integer position
) {
}
