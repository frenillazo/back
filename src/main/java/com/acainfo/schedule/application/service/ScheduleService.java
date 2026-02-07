package com.acainfo.schedule.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.schedule.application.dto.CreateScheduleCommand;
import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.application.dto.UpdateScheduleCommand;
import com.acainfo.schedule.application.port.in.CreateScheduleUseCase;
import com.acainfo.schedule.application.port.in.DeleteScheduleUseCase;
import com.acainfo.schedule.application.port.in.GetScheduleUseCase;
import com.acainfo.schedule.application.port.in.UpdateScheduleUseCase;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.exception.InvalidScheduleDataException;
import com.acainfo.schedule.domain.exception.ScheduleConflictException;
import com.acainfo.schedule.domain.exception.ScheduleNotFoundException;
import com.acainfo.schedule.domain.exception.TeacherScheduleConflictException;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Service implementing schedule use cases.
 * Contains business logic and validations for schedule operations.
 * Conflict detection is handled here, not in the repository layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService implements
        CreateScheduleUseCase,
        UpdateScheduleUseCase,
        GetScheduleUseCase,
        DeleteScheduleUseCase {

    private final ScheduleRepositoryPort scheduleRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;

    @Override
    @Transactional
    public Schedule create(CreateScheduleCommand command) {
        log.info("Creating schedule for group: {}, day: {}, time: {}-{}, classroom: {}",
                command.groupId(), command.dayOfWeek(), command.startTime(), command.endTime(), command.classroom());

        // Validate group exists and get group data
        SubjectGroup group = groupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        // Validate time range
        validateTimeRange(command.startTime(), command.endTime());

        // Check for classroom conflicts
        checkForConflicts(
                command.classroom(),
                command.dayOfWeek(),
                command.startTime(),
                command.endTime(),
                null  // No schedule to exclude (new schedule)
        );

        // Check for teacher conflicts
        checkForTeacherConflicts(
                group.getTeacherId(),
                group.getSubjectId(),
                command.dayOfWeek(),
                command.startTime(),
                command.endTime(),
                command.classroom(),
                null  // No schedule to exclude (new schedule)
        );

        // Create schedule
        Schedule schedule = Schedule.builder()
                .groupId(command.groupId())
                .dayOfWeek(command.dayOfWeek())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .classroom(command.classroom())
                .build();

        Schedule savedSchedule = scheduleRepositoryPort.save(schedule);

        log.info("Schedule created successfully: ID {}", savedSchedule.getId());
        return savedSchedule;
    }

    @Override
    @Transactional
    public Schedule update(Long id, UpdateScheduleCommand command) {
        log.info("Updating schedule with ID: {}", id);

        Schedule schedule = getById(id);

        // Get group data for teacher conflict validation
        SubjectGroup group = groupRepositoryPort.findById(schedule.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException(schedule.getGroupId()));

        // Determine final values (use existing if not provided)
        DayOfWeek finalDayOfWeek = command.dayOfWeek() != null ? command.dayOfWeek() : schedule.getDayOfWeek();
        LocalTime finalStartTime = command.startTime() != null ? command.startTime() : schedule.getStartTime();
        LocalTime finalEndTime = command.endTime() != null ? command.endTime() : schedule.getEndTime();
        Classroom finalClassroom = command.classroom() != null ? command.classroom() : schedule.getClassroom();

        // Validate time range
        validateTimeRange(finalStartTime, finalEndTime);

        // Check for classroom conflicts (excluding this schedule)
        checkForConflicts(finalClassroom, finalDayOfWeek, finalStartTime, finalEndTime, id);

        // Check for teacher conflicts (excluding this schedule)
        checkForTeacherConflicts(
                group.getTeacherId(),
                group.getSubjectId(),
                finalDayOfWeek,
                finalStartTime,
                finalEndTime,
                finalClassroom,
                id  // Exclude this schedule
        );

        // Apply updates
        if (command.dayOfWeek() != null) {
            schedule.setDayOfWeek(command.dayOfWeek());
        }
        if (command.startTime() != null) {
            schedule.setStartTime(command.startTime());
        }
        if (command.endTime() != null) {
            schedule.setEndTime(command.endTime());
        }
        if (command.classroom() != null) {
            schedule.setClassroom(command.classroom());
        }

        Schedule updatedSchedule = scheduleRepositoryPort.save(schedule);
        log.info("Schedule updated successfully: ID {}", id);

        return updatedSchedule;
    }

    @Override
    @Transactional(readOnly = true)
    public Schedule getById(Long id) {
        log.debug("Getting schedule by ID: {}", id);
        return scheduleRepositoryPort.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Schedule> findWithFilters(ScheduleFilters filters) {
        log.debug("Finding schedules with filters: groupId={}, classroom={}, dayOfWeek={}",
                filters.groupId(), filters.classroom(), filters.dayOfWeek());
        return scheduleRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findByGroupId(Long groupId) {
        log.debug("Finding schedules for group: {}", groupId);
        return scheduleRepositoryPort.findByGroupId(groupId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting schedule with ID: {}", id);

        if (!scheduleRepositoryPort.existsById(id)) {
            throw new ScheduleNotFoundException(id);
        }

        // Delete all sessions generated from this schedule before deleting the schedule itself
        sessionRepositoryPort.deleteByScheduleId(id);
        log.info("Deleted sessions associated with schedule ID: {}", id);

        scheduleRepositoryPort.delete(id);
        log.info("Schedule deleted successfully: ID {}", id);
    }

    // ==================== Helper Methods ====================

    /**
     * Validate that startTime is before endTime.
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidScheduleDataException("Start time and end time are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidScheduleDataException("Start time must be before end time");
        }
    }

    /**
     * Check for classroom conflicts.
     * A conflict exists when another schedule uses the same PHYSICAL classroom,
     * on the same day, with overlapping time.
     *
     * NOTE: Virtual classrooms (AULA_VIRTUAL) don't have conflicts -
     * multiple groups can use the virtual classroom simultaneously.
     */
    private void checkForConflicts(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeScheduleId
    ) {
        // Virtual classrooms don't have physical conflicts - skip this check
        if (classroom == Classroom.AULA_VIRTUAL) {
            log.debug("Skipping classroom conflict check for AULA_VIRTUAL");
            return;
        }

        // Get all schedules and filter for conflicts
        ScheduleFilters filters = new ScheduleFilters(
                null,  // all groups
                classroom,
                dayOfWeek,
                null, null, null, null  // default pagination
        );

        List<Schedule> potentialConflicts = scheduleRepositoryPort.findWithFilters(filters).getContent();

        for (Schedule existing : potentialConflicts) {
            // Skip the schedule being updated
            if (excludeScheduleId != null && excludeScheduleId.equals(existing.getId())) {
                continue;
            }

            // Check time overlap: start1 < end2 AND end1 > start2
            if (timeOverlaps(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                throw new ScheduleConflictException(
                        classroom,
                        dayOfWeek,
                        existing.getStartTime(),
                        existing.getEndTime()
                );
            }
        }
    }

    /**
     * Check if two time ranges overlap.
     */
    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Check for teacher schedule conflicts.
     * A teacher can have overlapping schedules ONLY if:
     * 1. Both schedules are online (AULA_VIRTUAL)
     * 2. Both schedules are for the same subject
     *
     * @param teacherId The teacher's ID
     * @param subjectId The subject ID for the new schedule
     * @param dayOfWeek The day of week
     * @param startTime Start time
     * @param endTime End time
     * @param classroom The classroom (to check if online)
     * @param excludeScheduleId Schedule ID to exclude (for updates)
     */
    private void checkForTeacherConflicts(
            Long teacherId,
            Long subjectId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Classroom classroom,
            Long excludeScheduleId
    ) {
        // Get all schedules for this teacher on this day
        List<Schedule> teacherSchedules = scheduleRepositoryPort.findByTeacherIdAndDayOfWeek(teacherId, dayOfWeek);

        boolean newScheduleIsOnline = classroom == Classroom.AULA_VIRTUAL;

        for (Schedule existing : teacherSchedules) {
            // Skip the schedule being updated
            if (excludeScheduleId != null && excludeScheduleId.equals(existing.getId())) {
                continue;
            }

            // Check time overlap
            if (!timeOverlaps(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                continue; // No overlap, no conflict
            }

            // There's a time overlap - check if it's allowed
            boolean existingScheduleIsOnline = existing.isOnline();

            // Get the subject ID of the existing schedule's group
            SubjectGroup existingGroup = groupRepositoryPort.findById(existing.getGroupId())
                    .orElse(null);
            if (existingGroup == null) {
                continue; // Skip if group not found (shouldn't happen)
            }

            boolean sameSubject = subjectId.equals(existingGroup.getSubjectId());
            boolean bothOnline = newScheduleIsOnline && existingScheduleIsOnline;

            log.info("Checking teacher conflict: newSubjectId={}, existingSubjectId={}, sameSubject={}, " +
                    "newClassroom={}, existingClassroom={}, newIsOnline={}, existingIsOnline={}, bothOnline={}",
                    subjectId, existingGroup.getSubjectId(), sameSubject,
                    classroom, existing.getClassroom(), newScheduleIsOnline, existingScheduleIsOnline, bothOnline);

            // Allow overlap ONLY if both are online AND same subject
            if (bothOnline && sameSubject) {
                log.info("Allowing teacher schedule overlap: both online and same subject (subjectId={})", subjectId);
                continue;
            }

            // Conflict! Get teacher name for the error message
            String teacherName = userRepositoryPort.findById(teacherId)
                    .map(user -> user.getFullName())
                    .orElse("ID " + teacherId);

            log.warn("Teacher schedule conflict detected: teacher={}, day={}, time={}-{} overlaps with existing schedule {}",
                    teacherName, dayOfWeek, startTime, endTime, existing.getId());

            throw new TeacherScheduleConflictException(
                    teacherName,
                    dayOfWeek,
                    existing.getStartTime(),
                    existing.getEndTime()
            );
        }
    }
}
