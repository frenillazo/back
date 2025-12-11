package com.acainfo.session.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * REST DTO for generating sessions from schedules.
 * Request body for POST /api/sessions/generate
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GenerateSessionsRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
