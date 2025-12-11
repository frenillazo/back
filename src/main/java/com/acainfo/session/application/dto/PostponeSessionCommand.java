package com.acainfo.session.application.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command DTO for postponing a session.
 * Contains the new date/time information for the postponed session.
 */
public record PostponeSessionCommand(
        LocalDate newDate,
        LocalTime newStartTime,  // Optional: null = keep original time
        LocalTime newEndTime,    // Optional: null = keep original time
        Classroom newClassroom,  // Optional: null = keep original classroom
        SessionMode newMode      // Optional: null = keep original mode
) {
    /**
     * Simple postpone to a new date keeping the same time and location.
     */
    public static PostponeSessionCommand toDate(LocalDate newDate) {
        return new PostponeSessionCommand(newDate, null, null, null, null);
    }

    /**
     * Postpone with new date and time.
     */
    public static PostponeSessionCommand toDateTime(
            LocalDate newDate,
            LocalTime newStartTime,
            LocalTime newEndTime
    ) {
        return new PostponeSessionCommand(newDate, newStartTime, newEndTime, null, null);
    }
}
