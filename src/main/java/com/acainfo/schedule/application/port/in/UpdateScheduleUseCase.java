package com.acainfo.schedule.application.port.in;

import com.acainfo.schedule.application.dto.UpdateScheduleCommand;
import com.acainfo.schedule.domain.model.Schedule;

/**
 * Use case for updating schedules.
 * Input port defining the contract for schedule updates.
 */
public interface UpdateScheduleUseCase {

    /**
     * Update an existing schedule.
     *
     * @param id Schedule ID
     * @param command Update data
     * @return The updated schedule
     */
    Schedule update(Long id, UpdateScheduleCommand command);
}
