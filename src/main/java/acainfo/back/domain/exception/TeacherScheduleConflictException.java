package acainfo.back.domain.exception;

import acainfo.back.domain.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Exception thrown when a teacher has conflicting schedules.
 * A teacher cannot be in two places at the same time.
 */
public class TeacherScheduleConflictException extends ScheduleConflictException {

    private final Long teacherId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TeacherScheduleConflictException(
            Long teacherId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            List<Schedule> conflictingSchedules
    ) {
        super(buildMessage(teacherId, dayOfWeek, startTime, endTime, conflictingSchedules.size()),
                conflictingSchedules);
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private static String buildMessage(
            Long teacherId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            int conflictCount
    ) {
        return String.format(
                "Teacher with ID %d has %d conflicting schedule(s) on %s from %s to %s. " +
                "A teacher cannot be in multiple places at the same time.",
                teacherId, conflictCount, dayOfWeek, startTime, endTime
        );
    }

    public Long getTeacherId() {
        return teacherId;
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
