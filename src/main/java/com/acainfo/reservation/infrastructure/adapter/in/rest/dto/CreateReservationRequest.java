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

    @NotNull(message = "El ID de estudiante es obligatorio")
    private Long studentId;

    @NotNull(message = "El ID de sesión es obligatorio")
    private Long sessionId;

    @NotNull(message = "El ID de inscripción es obligatorio")
    private Long enrollmentId;

    @NotNull(message = "El modo es obligatorio")
    private ReservationMode mode;
}
