package acainfo.back.schedule.application.ports.in;

import acainfo.back.schedule.domain.model.ScheduleDomain;

/**
 * Use case interface for managing schedules (create, update, delete operations).
 * Works with ScheduleDomain (pure domain model)
 */
public interface ManageScheduleUseCase {

    /**
     * Creates a new schedule.
     *
     * @param schedule the schedule to create
     * @return the created schedule with ID
     */
    ScheduleDomain createSchedule(ScheduleDomain schedule);

    /**
     * Updates an existing schedule.
     *
     * @param scheduleId the ID of the schedule to update
     * @param schedule the schedule data
     * @return the updated schedule
     */
    ScheduleDomain updateSchedule(Long scheduleId, ScheduleDomain schedule);

    /**
     * Deletes a schedule by ID.
     *
     * @param scheduleId the ID of the schedule to delete
     */
    void deleteSchedule(Long scheduleId);

    /**
     * Deletes all schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     */
    void deleteSchedulesByGroupId(Long groupId);
}
