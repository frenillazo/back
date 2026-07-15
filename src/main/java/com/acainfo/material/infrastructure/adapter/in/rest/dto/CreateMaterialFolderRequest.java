package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/subjects/{subjectId}/material-folders.
 */
public record CreateMaterialFolderRequest(
        @NotBlank(message = "El nombre de la carpeta es obligatorio")
        @Size(max = 100, message = "El nombre de la carpeta no puede exceder 100 caracteres")
        String name
) {
}
