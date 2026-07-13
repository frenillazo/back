package com.acainfo.schedule.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a schedule is not found.
 */
public class ScheduleNotFoundException extends NotFoundException {

    public ScheduleNotFoundException(Long scheduleId) {
        super("Horario no encontrado con id: " + scheduleId);
    }
}
