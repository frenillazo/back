package com.acainfo.session.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * REST DTO for creating a new session.
 * Request body for POST /api/sessions
 *
 * <p>Field requirements by session type:</p>
 * <ul>
 *   <li>REGULAR: scheduleId required, courseId optional (derived from schedule)</li>
 *   <li>EXTRA: courseId required</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateSessionRequest {

    @NotNull(message = "El tipo de sesión es obligatorio")
    private SessionType type;

    private Long subjectId;   // Optional: derived from the course

    private Long courseId;     // Required for EXTRA, optional for REGULAR

    private Long scheduleId;  // Required for REGULAR

    @NotNull(message = "El aula es obligatoria")
    private Classroom classroom;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @NotNull(message = "El modo de sesión es obligatorio")
    private SessionMode mode;
}
