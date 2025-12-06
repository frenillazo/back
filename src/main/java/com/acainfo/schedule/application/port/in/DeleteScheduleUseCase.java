package com.acainfo.schedule.application.port.in;

/**
 * Use case for deleting schedules.
 * Input port defining the contract for schedule deletion.
 */
public interface DeleteScheduleUseCase {

    /**
     * Delete a schedule.
     *
     * @param id Schedule ID
     */
    void delete(Long id);
}
