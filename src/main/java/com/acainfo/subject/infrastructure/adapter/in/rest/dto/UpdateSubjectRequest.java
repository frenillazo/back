package com.acainfo.subject.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.SubjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for updating a subject.
 * All fields are optional.
 * clearYear=true sets year to null ("sin asignar") and takes precedence over year.
 * Maps to UpdateSubjectCommand in application layer.
 */
public record UpdateSubjectRequest(

        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @Min(value = 1, message = "El curso debe estar entre 1 y 4")
        @Max(value = 4, message = "El curso debe estar entre 1 y 4")
        Integer year,

        Boolean clearYear,

        SubjectStatus status
) {
}
