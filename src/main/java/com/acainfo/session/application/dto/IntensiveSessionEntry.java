package com.acainfo.session.application.dto;

import com.acainfo.schedule.domain.model.Classroom;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One date/time/classroom entry for creating/editing intensive sessions.
 */
public record IntensiveSessionEntry(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Classroom classroom
) {
}
