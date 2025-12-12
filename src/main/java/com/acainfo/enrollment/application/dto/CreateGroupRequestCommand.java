package com.acainfo.enrollment.application.dto;

import com.acainfo.group.domain.model.GroupType;

/**
 * Command DTO for creating a new group request.
 */
public record CreateGroupRequestCommand(
        Long subjectId,
        Long requesterId,
        GroupType requestedGroupType,
        String justification
) {
}
