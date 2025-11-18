package acainfo.back.schedule.application.ports.in;

import acainfo.back.schedule.domain.model.Schedule;

/**
 * Use case interface for managing schedules (create, update, delete operations).
 */
public interface ManageScheduleUseCase {

    /**
     * Creates a new schedule.
     *
     * @param schedule the schedule to create
     * @return the created schedule with ID
     */
    Schedule createSchedule(Schedule schedule);

    /**
     * Updates an existing schedule.
     *
     * @param scheduleId the ID of the schedule to update
     * @param schedule the schedule data
     * @return the updated schedule
     */
    Schedule updateSchedule(Long scheduleId, Schedule schedule);

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
