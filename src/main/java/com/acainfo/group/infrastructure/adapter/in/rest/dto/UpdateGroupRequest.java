package com.acainfo.group.infrastructure.adapter.in.rest.dto;

import com.acainfo.group.domain.model.GroupStatus;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * REST DTO for updating an existing group.
 * Request body for PUT /api/groups/{id}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateGroupRequest {

    private GroupStatus status;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}
