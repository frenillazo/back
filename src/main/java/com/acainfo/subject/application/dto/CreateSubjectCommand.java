package com.acainfo.subject.application.dto;

import com.acainfo.subject.domain.model.Degree;

/**
 * Command DTO for creating a subject.
 */
public record CreateSubjectCommand(
        String code,
        String name,
        String description,
        Integer credits,
        Degree degree
) {
}
