package acainfo.back.schedule.application.services;

import acainfo.back.schedule.domain.exception.ClassroomScheduleConflictException;
import acainfo.back.schedule.domain.exception.TeacherScheduleConflictException;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.schedule.application.ports.out.ScheduleRepositoryPort;
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

    private final ScheduleRepositoryPort scheduleRepository;

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

        List<ScheduleDomain> conflicts = scheduleRepository.findTeacherConflicts(
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

        List<ScheduleDomain> conflicts = scheduleRepository.findClassroomConflicts(
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
     * @param teacherId Teacher ID (from subject group)
     * @param excludeScheduleId Schedule ID to exclude from conflict check (for updates)
     * @throws TeacherScheduleConflictException if teacher conflicts are found
     * @throws ClassroomScheduleConflictException if classroom conflicts are found
     */
    public void validateSchedule(ScheduleDomain schedule, Long teacherId, Long excludeScheduleId) {
        log.debug("Validating schedule: groupId={}, day={}, time={}-{}, classroom={}, teacherId={}",
                schedule.getSubjectGroupId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getClassroom(),
                teacherId
        );

        // Validate teacher availability
        if (teacherId != null) {
            validateTeacherAvailability(
                    teacherId,
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

        log.debug("Schedule validation passed");
    }
}
