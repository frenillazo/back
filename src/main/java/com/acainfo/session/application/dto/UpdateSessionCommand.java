package com.acainfo.session.application.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command DTO for updating a session.
 * All fields are optional (null = no change).
 *
 * <p>Note: Session type cannot be changed after creation.
 * Status changes should use dedicated lifecycle methods (cancel, complete, postpone).</p>
 */
public record UpdateSessionCommand(
        Classroom classroom,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        SessionMode mode
) {
}
