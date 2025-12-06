package com.acainfo.schedule.application.port.out;

import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import org.springframework.data.domain.Page;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port for Schedule persistence.
 * Defines the contract for Schedule repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface ScheduleRepositoryPort {

    /**
     * Save or update a schedule.
     *
     * @param schedule Domain schedule to persist
     * @return Persisted schedule with ID
     */
    Schedule save(Schedule schedule);

    /**
     * Find schedule by ID.
     *
     * @param id Schedule ID
     * @return Optional containing the schedule if found
     */
    Optional<Schedule> findById(Long id);

    /**
     * Find schedules with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of schedules matching filters
     */
    Page<Schedule> findWithFilters(ScheduleFilters filters);

    /**
     * Find all schedules for a specific group.
     *
     * @param groupId Group ID
     * @return List of schedules for the group
     */
    List<Schedule> findByGroupId(Long groupId);

    /**
     * Find schedules by classroom and day of week.
     * Useful for conflict detection.
     *
     * @param classroom Classroom to check
     * @param dayOfWeek Day of week to check
     * @return List of schedules in that classroom on that day
     */
    List<Schedule> findByClassroomAndDayOfWeek(Classroom classroom, DayOfWeek dayOfWeek);

    /**
     * Find potential conflicting schedules.
     * Returns schedules in the same classroom, same day, that overlap with the given time range.
     *
     * @param classroom Classroom to check
     * @param dayOfWeek Day of week
     * @param startTime Start time of the range
     * @param endTime End time of the range
     * @param excludeScheduleId Schedule ID to exclude (for updates), can be null
     * @return List of conflicting schedules
     */
    List<Schedule> findConflictingSchedules(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeScheduleId
    );

    /**
     * Delete a schedule by ID.
     *
     * @param id Schedule ID
     */
    void delete(Long id);

    /**
     * Check if a schedule exists by ID.
     *
     * @param id Schedule ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
}
