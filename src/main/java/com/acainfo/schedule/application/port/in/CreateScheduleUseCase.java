package com.acainfo.schedule.application.port.in;

import com.acainfo.schedule.application.dto.CreateScheduleCommand;
import com.acainfo.schedule.domain.model.Schedule;

/**
 * Use case for creating schedules.
 * Input port defining the contract for schedule creation.
 */
public interface CreateScheduleUseCase {

    /**
     * Create a new schedule.
     *
     * @param command Schedule creation data
     * @return The created schedule
     */
    Schedule create(CreateScheduleCommand command);
}
