package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.Degree;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for user registration requests.
 * Maps to RegisterUserCommand in application layer.
 */
public record RegisterRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String firstName,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
        String lastName,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String phoneNumber,

        @NotNull(message = "La titulación es obligatoria")
        Degree degree
) {
}
