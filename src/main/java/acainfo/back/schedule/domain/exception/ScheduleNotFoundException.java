package acainfo.back.schedule.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when a requested schedule is not found.
 */
public class ScheduleNotFoundException extends DomainException {

    public ScheduleNotFoundException(Long scheduleId) {
        super(String.format("Schedule with ID %d not found", scheduleId));
    }

    public ScheduleNotFoundException(String message) {
        super(message);
    }
}
