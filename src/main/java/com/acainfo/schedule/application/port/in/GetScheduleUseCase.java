package com.acainfo.schedule.application.port.in;

import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.domain.model.Schedule;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Use case for retrieving schedules.
 * Input port defining the contract for schedule queries.
 */
public interface GetScheduleUseCase {

    /**
     * Get a schedule by ID.
     *
     * @param id Schedule ID
     * @return The schedule
     * @throws com.acainfo.schedule.domain.exception.ScheduleNotFoundException if not found
     */
    Schedule getById(Long id);

    /**
     * Find schedules with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of schedules matching the filters
     */
    Page<Schedule> findWithFilters(ScheduleFilters filters);

    /**
     * Get all schedules for a specific group.
     *
     * @param groupId Group ID
     * @return List of schedules for the group
     */
    List<Schedule> findByGroupId(Long groupId);
}
