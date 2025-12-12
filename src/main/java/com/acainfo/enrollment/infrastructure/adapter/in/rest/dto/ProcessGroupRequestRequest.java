package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * REST DTO for processing (approve/reject) a group request.
 * Request body for PUT /api/group-requests/{id}/approve or /reject
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessGroupRequestRequest {

    @NotNull(message = "Admin ID is required")
    private Long adminId;

    @Size(max = 500, message = "Admin response must not exceed 500 characters")
    private String adminResponse;
}
