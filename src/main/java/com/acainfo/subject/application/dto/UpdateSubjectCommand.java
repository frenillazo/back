package com.acainfo.subject.application.dto;

import com.acainfo.subject.domain.model.SubjectStatus;

/**
 * Command DTO for updating a subject.
 * All fields are optional (can be null).
 */
public record UpdateSubjectCommand(
        String name,
        String description,
        Integer credits,
        SubjectStatus status
) {
}
