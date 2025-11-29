package acainfo.back.schedule.domain.exception;

import acainfo.back.config.exception.DomainException;
import acainfo.back.schedule.domain.model.Schedule;

import java.util.List;

/**
 * Exception thrown when a schedule conflict is detected.
 */
public class ScheduleConflictException extends DomainException {

    private final List<Schedule> conflictingSchedules;

    public ScheduleConflictException(String message, List<Schedule> conflictingSchedules) {
        super(message);
        this.conflictingSchedules = conflictingSchedules;
    }

    public ScheduleConflictException(String message) {
        super(message);
        this.conflictingSchedules = List.of();
    }

    public List<Schedule> getConflictingSchedules() {
        return conflictingSchedules;
    }

    public int getConflictCount() {
        return conflictingSchedules.size();
    }
}
