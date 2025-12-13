package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for processing an online attendance request.
 * Request body for PUT /api/reservations/{id}/online-request/process
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessOnlineRequestRequest {

    @NotNull(message = "Approval decision is required")
    private Boolean approved;
}
