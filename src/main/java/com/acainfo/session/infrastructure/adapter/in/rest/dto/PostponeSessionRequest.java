package com.acainfo.session.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * REST DTO for postponing a session.
 * Request body for POST /api/sessions/{id}/postpone
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PostponeSessionRequest {

    @NotNull(message = "New date is required")
    private LocalDate newDate;

    private LocalTime newStartTime;  // Optional: null = keep original

    private LocalTime newEndTime;    // Optional: null = keep original

    private Classroom newClassroom;  // Optional: null = keep original

    private SessionMode newMode;     // Optional: null = keep original
}
