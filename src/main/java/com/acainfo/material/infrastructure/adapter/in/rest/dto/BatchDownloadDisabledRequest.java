package com.acainfo.material.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for PATCH /api/materials/batch/download-disabled.
 */
public record BatchDownloadDisabledRequest(
        @NotEmpty(message = "Debe indicar al menos un material")
        List<Long> ids,

        @NotNull(message = "El campo 'disabled' es obligatorio")
        Boolean disabled
) {
}
