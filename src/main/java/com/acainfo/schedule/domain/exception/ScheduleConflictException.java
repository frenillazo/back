package com.acainfo.schedule.domain.exception;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.shared.domain.exception.BusinessRuleException;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Exception thrown when a schedule conflicts with an existing schedule.
 * Conflict occurs when same classroom, same day, and overlapping time.
 */
public class ScheduleConflictException extends BusinessRuleException {

    public ScheduleConflictException(Classroom classroom, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        super(String.format(
                "Schedule conflict: %s is already booked on %s between %s and %s",
                classroom.getDisplayName(),
                dayOfWeek,
                startTime,
                endTime
        ));
    }

    public ScheduleConflictException(Long conflictingScheduleId) {
        super("Schedule conflicts with existing schedule id: " + conflictingScheduleId);
    }

    public ScheduleConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "SCHEDULE_CONFLICT";
    }
}
