package acainfo.back.domain.exception;

import acainfo.back.domain.model.Classroom;
import acainfo.back.domain.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Exception thrown when a classroom has conflicting schedules.
 * A classroom cannot host two different groups at the same time.
 */
public class ClassroomScheduleConflictException extends ScheduleConflictException {

    private final Classroom classroom;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public ClassroomScheduleConflictException(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            List<Schedule> conflictingSchedules
    ) {
        super(buildMessage(classroom, dayOfWeek, startTime, endTime, conflictingSchedules.size()),
                conflictingSchedules);
        this.classroom = classroom;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private static String buildMessage(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            int conflictCount
    ) {
        return String.format(
                "Classroom %s has %d conflicting schedule(s) on %s from %s to %s. " +
                "A classroom cannot host multiple groups simultaneously.",
                classroom.getDisplayName(), conflictCount, dayOfWeek, startTime, endTime
        );
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
