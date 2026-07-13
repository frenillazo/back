package com.acainfo.session.domain.exception;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.shared.domain.exception.ValidationException;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class SessionConflictException extends ValidationException {
    public SessionConflictException(Classroom classroom, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        super(String.format(
                "Conflicto de sesión: %s ya está reservada el %s entre %s y %s",
                classroom.getDisplayName(),
                dayOfWeek,
                startTime,
                endTime
        ));
    }

    public SessionConflictException(Long conflictingSessionId) {
        super("La sesión entra en conflicto con la sesión existente id: " + conflictingSessionId);
    }

    public SessionConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "SESSION_CONFLICT";
    }
}
