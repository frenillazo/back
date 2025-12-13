package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import com.acainfo.reservation.domain.model.ReservationMode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for creating a session reservation.
 * Request body for POST /api/reservations
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotNull(message = "Mode is required")
    private ReservationMode mode;
}
