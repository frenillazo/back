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

    @NotNull(message = "El ID de grupo es obligatorio")
    private Long courseId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;
}
