package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for updating teacher (ADMIN only).
 * Maps to UpdateTeacherCommand in application layer.
 */
public record UpdateTeacherRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String firstName,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
        String lastName
) {
}
