package com.acainfo.schedule.domain.exception;

import com.acainfo.shared.domain.exception.ValidationException;

/**
 * Exception thrown when schedule data is invalid.
 * Examples: startTime after endTime, null required fields.
 */
public class InvalidScheduleDataException extends ValidationException {

    public InvalidScheduleDataException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "SCHEDULE_INVALID_DATA";
    }
}
