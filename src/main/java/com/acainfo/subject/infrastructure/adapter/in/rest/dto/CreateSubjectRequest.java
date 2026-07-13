package com.acainfo.subject.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.Degree;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for creating a subject.
 * Maps to CreateSubjectCommand in application layer.
 */
public record CreateSubjectRequest(

        @NotBlank(message = "El código de asignatura es obligatorio")
        @Pattern(regexp = "^[A-Z]{3}\\d{3}$", message = "El código debe ser 3 letras mayúsculas seguidas de 3 dígitos (p.ej., ING101)")
        String code,

        @NotBlank(message = "El nombre de la asignatura es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @NotNull(message = "La titulación es obligatoria")
        Degree degree,

        @Min(value = 1, message = "El curso debe estar entre 1 y 4")
        @Max(value = 4, message = "El curso debe estar entre 1 y 4")
        Integer year
) {
}
