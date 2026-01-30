package com.acainfo.subject.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.SubjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for updating a subject.
 * All fields are optional.
 * Maps to UpdateSubjectCommand in application layer.
 */
public record UpdateSubjectRequest(

        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Min(value = 1, message = "Year must be between 1 and 4")
        @Max(value = 4, message = "Year must be between 1 and 4")
        Integer year,

        SubjectStatus status
) {
}
