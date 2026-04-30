package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * REST DTO for creating a (regular) group request.
 * Request body for POST /api/group-requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupRequestRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @Size(max = 500, message = "Justification must not exceed 500 characters")
    private String justification;
}
