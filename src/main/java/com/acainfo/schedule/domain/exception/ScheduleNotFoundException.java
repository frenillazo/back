package com.acainfo.schedule.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a schedule is not found.
 */
public class ScheduleNotFoundException extends NotFoundException {

    public ScheduleNotFoundException(Long scheduleId) {
        super("Schedule not found with id: " + scheduleId);
    }
}
