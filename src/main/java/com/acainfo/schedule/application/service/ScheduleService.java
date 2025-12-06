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

    @Override
    @Transactional
    public Schedule create(CreateScheduleCommand command) {
        log.info("Creating schedule for group: {}, day: {}, time: {}-{}, classroom: {}",
                command.groupId(), command.dayOfWeek(), command.startTime(), command.endTime(), command.classroom());

        // Validate group exists
        validateGroupExists(command.groupId());

        // Validate time range
        validateTimeRange(command.startTime(), command.endTime());

        // Check for conflicts
        checkForConflicts(
                command.classroom(),
                command.dayOfWeek(),
                command.startTime(),
                command.endTime(),
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

        // Determine final values (use existing if not provided)
        DayOfWeek finalDayOfWeek = command.dayOfWeek() != null ? command.dayOfWeek() : schedule.getDayOfWeek();
        LocalTime finalStartTime = command.startTime() != null ? command.startTime() : schedule.getStartTime();
        LocalTime finalEndTime = command.endTime() != null ? command.endTime() : schedule.getEndTime();
        Classroom finalClassroom = command.classroom() != null ? command.classroom() : schedule.getClassroom();

        // Validate time range
        validateTimeRange(finalStartTime, finalEndTime);

        // Check for conflicts (excluding this schedule)
        checkForConflicts(finalClassroom, finalDayOfWeek, finalStartTime, finalEndTime, id);

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

        scheduleRepositoryPort.delete(id);
        log.info("Schedule deleted successfully: ID {}", id);
    }

    // ==================== Helper Methods ====================

    /**
     * Validate that the group exists.
     */
    private void validateGroupExists(Long groupId) {
        if (!groupRepositoryPort.findById(groupId).isPresent()) {
            throw new GroupNotFoundException(groupId);
        }
    }

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
     * A conflict exists when another schedule uses the same classroom,
     * on the same day, with overlapping time.
     */
    private void checkForConflicts(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeScheduleId
    ) {
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
}
