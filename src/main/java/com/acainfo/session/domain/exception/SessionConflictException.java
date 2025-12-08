package com.acainfo.session.domain.exception;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.shared.domain.exception.ValidationException;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class SessionConflictException extends ValidationException {
    public SessionConflictException(Classroom classroom, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        super(String.format(
                "Session conflict: %s is already booked on %s between %s and %s",
                classroom.getDisplayName(),
                dayOfWeek,
                startTime,
                endTime
        ));
    }

    public SessionConflictException(Long conflictingSessionId) {
        super("Session conflicts with existing session id: " + conflictingSessionId);
    }

    public SessionConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "SESSION_CONFLICT";
    }
}
