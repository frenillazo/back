package com.acainfo.subject.application.dto;

import com.acainfo.subject.domain.model.SubjectStatus;

/**
 * Command DTO for updating a subject.
 * All fields are optional (can be null).
 * clearYear=true sets year to null ("sin asignar") and takes precedence over year.
 */
public record UpdateSubjectCommand(
        String name,
        Integer year,
        Boolean clearYear,
        SubjectStatus status
) {
}
