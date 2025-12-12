package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for changing a student's group.
 * Request body for PUT /api/enrollments/{id}/change-group
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeGroupRequest {

    @NotNull(message = "New group ID is required")
    private Long newGroupId;
}
