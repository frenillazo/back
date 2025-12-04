package com.acainfo.subject.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.SubjectStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * REST DTO for subject responses.
 * Contains all subject information for API responses.
 */
@Builder
public record SubjectResponse(
        Long id,
        String code,
        String name,
        String displayName,
        Degree degree,
        SubjectStatus status,
        Integer currentGroupCount,
        Integer remainingGroupSlots,
        boolean active,
        boolean archived,
        boolean canCreateGroup,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
