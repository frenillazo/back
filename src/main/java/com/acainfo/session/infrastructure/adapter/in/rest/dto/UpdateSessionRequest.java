package com.acainfo.session.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * REST DTO for updating a session.
 * Request body for PUT /api/sessions/{id}
 * All fields are optional (null = no change).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateSessionRequest {

    private Classroom classroom;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private SessionMode mode;
}
