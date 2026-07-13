package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for updating user profile.
 * Maps to UpdateUserCommand in application layer.
 */
public record UpdateProfileRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String firstName,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
        String lastName,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String phoneNumber
) {
}
