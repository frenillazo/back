package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for adding a supporter to a group request.
 * Request body for POST /api/group-requests/{id}/support
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSupporterRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;
}
