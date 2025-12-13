package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for switching to a different session.
 * Request body for PUT /api/reservations/{id}/switch-session
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwitchSessionRequest {

    @NotNull(message = "New session ID is required")
    private Long newSessionId;
}
