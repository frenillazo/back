package acainfo.back.schedule.application.services;

import acainfo.back.schedule.domain.exception.ClassroomScheduleConflictException;
import acainfo.back.schedule.domain.exception.TeacherScheduleConflictException;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.Schedule;
import acainfo.back.schedule.infrastructure.adapters.out.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Service for validating schedule conflicts.
 * Ensures that teachers and classrooms are not double-booked.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleValidationService {

    private final ScheduleRepository scheduleRepository;

    /**
     * Validates that a teacher is available for the proposed schedule.
     * A teacher cannot be in two places at the same time.
     *
     * @param teacherId Teacher ID to validate
     * @param dayOfWeek Day of the week
     * @param startTime Start time of the schedule
     * @param endTime End time of the schedule
     * @param excludeScheduleId Schedule ID to exclude from conflict check (for updates)
     * @throws TeacherScheduleConflictException if conflicts are found
     */
    public void validateTeacherAvailability(
            Long teacherId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeScheduleId
    ) {
        if (teacherId == null) {
            log.debug("No teacher assigned, skipping teacher availability validation");
            return;
        }

        log.debug("Validating teacher availability: teacherId={}, day={}, time={}-{}",
                teacherId, dayOfWeek, startTime, endTime);

        List<Schedule> conflicts = scheduleRepository.findTeacherConflicts(
                teacherId, dayOfWeek, startTime, endTime
        );

        // Exclude the current schedule if we're updating
        if (excludeScheduleId != null) {
            conflicts = conflicts.stream()
                    .filter(s -> !s.getId().equals(excludeScheduleId))
                    .toList();
        }

        if (!conflicts.isEmpty()) {
            log.warn("Teacher {} has {} conflicting schedule(s) on {} from {} to {}",
                    teacherId, conflicts.size(), dayOfWeek, startTime, endTime);

            throw new TeacherScheduleConflictException(
                    teacherId,
                    dayOfWeek,
                    startTime,
                    endTime,
                    conflicts
            );
        }

        log.debug("Teacher availability validated successfully");
    }

    /**
     * Validates that a classroom is available for the proposed schedule.
     * A classroom cannot host multiple groups simultaneously.
     *
     * @param classroom Classroom to validate
     * @param dayOfWeek Day of the week
     * @param startTime Start time of the schedule
     * @param endTime End time of the schedule
     * @param excludeScheduleId Schedule ID to exclude from conflict check (for updates)
     * @throws ClassroomScheduleConflictException if conflicts are found
     */
    public void validateClassroomAvailability(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeScheduleId
    ) {
        if (classroom == null) {
            log.debug("No classroom assigned, skipping classroom availability validation");
            return;
        }

        // Virtual classrooms don't have capacity conflicts
        if (classroom.isVirtual()) {
            log.debug("Virtual classroom, skipping physical availability validation");
            return;
        }

        log.debug("Validating classroom availability: classroom={}, day={}, time={}-{}",
                classroom, dayOfWeek, startTime, endTime);

        List<Schedule> conflicts = scheduleRepository.findClassroomConflicts(
                classroom, dayOfWeek, startTime, endTime
        );

        // Exclude the current schedule if we're updating
        if (excludeScheduleId != null) {
            conflicts = conflicts.stream()
                    .filter(s -> !s.getId().equals(excludeScheduleId))
                    .toList();
        }

        if (!conflicts.isEmpty()) {
            log.warn("Classroom {} has {} conflicting schedule(s) on {} from {} to {}",
                    classroom.getDisplayName(), conflicts.size(), dayOfWeek, startTime, endTime);

            throw new ClassroomScheduleConflictException(
                    classroom,
                    dayOfWeek,
                    startTime,
                    endTime,
                    conflicts
            );
        }

        log.debug("Classroom availability validated successfully");
    }

    /**
     * Validates a complete schedule for both teacher and classroom availability.
     * This is a convenience method that combines both validations.
     *
     * @param schedule Schedule to validate
     * @param excludeScheduleId Schedule ID to exclude from conflict check (for updates)
     * @throws TeacherScheduleConflictException if teacher conflicts are found
     * @throws ClassroomScheduleConflictException if classroom conflicts are found
     */
    public void validateSchedule(Schedule schedule, Long excludeScheduleId) {
        log.debug("Validating schedule: groupId={}, day={}, time={}-{}, classroom={}, teacherId={}",
                schedule.getSubjectGroup().getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getClassroom(),
                schedule.getSubjectGroup().getTeacher() != null ? schedule.getSubjectGroup().getTeacher().getId() : null
        );

        // Validate teacher availability
        if (schedule.getSubjectGroup().getTeacher() != null) {
            validateTeacherAvailability(
                    schedule.getSubjectGroup().getTeacher().getId(),
                    schedule.getDayOfWeek(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    excludeScheduleId
            );
        }

        // Validate classroom availability
        validateClassroomAvailability(
                schedule.getClassroom(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                excludeScheduleId
        );

        log.debug("Schedule validation completed successfully");
    }

    /**
     * Validates a new schedule (convenience method for creation).
     *
     * @param schedule Schedule to validate
     */
    public void validateNewSchedule(Schedule schedule) {
        validateSchedule(schedule, null);
    }

    /**
     * Checks if a teacher is available without throwing an exception.
     * Useful for checking availability before attempting to create a schedule.
     *
     * @param teacherId Teacher ID to check
     * @param dayOfWeek Day of the week
     * @param startTime Start time
     * @param endTime End time
     * @return true if available, false if conflicts exist
     */
    public boolean isTeacherAvailable(
            Long teacherId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (teacherId == null) {
            return true;
        }

        List<Schedule> conflicts = scheduleRepository.findTeacherConflicts(
                teacherId, dayOfWeek, startTime, endTime
        );

        return conflicts.isEmpty();
    }

    /**
     * Checks if a classroom is available without throwing an exception.
     * Useful for checking availability before attempting to create a schedule.
     *
     * @param classroom Classroom to check
     * @param dayOfWeek Day of the week
     * @param startTime Start time
     * @param endTime End time
     * @return true if available, false if conflicts exist
     */
    public boolean isClassroomAvailable(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (classroom == null || classroom.isVirtual()) {
            return true;
        }

        List<Schedule> conflicts = scheduleRepository.findClassroomConflicts(
                classroom, dayOfWeek, startTime, endTime
        );

        return conflicts.isEmpty();
    }

    /**
     * Gets all conflicting schedules for a teacher at a given time.
     *
     * @param teacherId Teacher ID
     * @param dayOfWeek Day of the week
     * @param startTime Start time
     * @param endTime End time
     * @return List of conflicting schedules
     */
    public List<Schedule> getTeacherConflicts(
            Long teacherId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return scheduleRepository.findTeacherConflicts(teacherId, dayOfWeek, startTime, endTime);
    }

    /**
     * Gets all conflicting schedules for a classroom at a given time.
     *
     * @param classroom Classroom
     * @param dayOfWeek Day of the week
     * @param startTime Start time
     * @param endTime End time
     * @return List of conflicting schedules
     */
    public List<Schedule> getClassroomConflicts(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return scheduleRepository.findClassroomConflicts(classroom, dayOfWeek, startTime, endTime);
    }
}
