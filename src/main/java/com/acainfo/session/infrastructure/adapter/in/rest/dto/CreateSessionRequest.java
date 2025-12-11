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
 *   <li>REGULAR: scheduleId required, groupId optional (derived from schedule)</li>
 *   <li>EXTRA: groupId required</li>
 *   <li>SCHEDULING: subjectId required</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateSessionRequest {

    @NotNull(message = "Session type is required")
    private SessionType type;

    private Long subjectId;   // Required for SCHEDULING

    private Long groupId;     // Required for EXTRA, optional for REGULAR

    private Long scheduleId;  // Required for REGULAR

    @NotNull(message = "Classroom is required")
    private Classroom classroom;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Session mode is required")
    private SessionMode mode;
}
