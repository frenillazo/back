package com.acainfo.group.infrastructure.adapter.in.rest.dto;

import com.acainfo.group.domain.model.GroupType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for creating a new group.
 * Request body for POST /api/groups
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateGroupRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Group type is required")
    private GroupType type;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;  // null = use default based on type
}
