package com.acainfo.subject.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.SubjectStatus;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for updating a subject.
 * All fields are optional.
 * Maps to UpdateSubjectCommand in application layer.
 */
public record UpdateSubjectRequest(

        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        SubjectStatus status
) {
}
