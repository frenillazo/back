package com.acainfo.enrollment.application.dto;

/**
 * Command DTO for creating a new (regular) group request.
 */
public record CreateGroupRequestCommand(
        Long subjectId,
        Long requesterId,
        String justification
) {
}
