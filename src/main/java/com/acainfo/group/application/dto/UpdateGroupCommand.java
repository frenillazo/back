package com.acainfo.group.application.dto;

import com.acainfo.group.domain.model.GroupStatus;

/**
 * Command DTO for updating a group.
 * All fields are optional (null = no change).
 */
public record UpdateGroupCommand(
        Integer capacity,
        GroupStatus status
) {
}
