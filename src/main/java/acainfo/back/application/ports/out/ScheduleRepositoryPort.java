package acainfo.back.application.ports.out;

import acainfo.back.domain.model.Classroom;
import acainfo.back.domain.model.Schedule;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for Schedule repository operations.
 */
public interface ScheduleRepositoryPort {

    /**
     * Saves a schedule.
     *
     * @param schedule the schedule to save
     * @return the saved schedule
     */
    Schedule save(Schedule schedule);

    /**
     * Finds a schedule by ID.
     *
     * @param scheduleId the schedule ID
     * @return Optional containing the schedule if found
     */
    Optional<Schedule> findById(Long scheduleId);

    /**
     * Finds all schedules.
     *
     * @return list of all schedules
     */
    List<Schedule> findAll();

    /**
     * Finds all schedules for a specific group.
     *
     * @param groupId the group ID
     * @return list of schedules
     */
    List<Schedule> findByGroupId(Long groupId);

    /**
     * Finds all schedules for a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return list of schedules
     */
    List<Schedule> findByTeacherId(Long teacherId);

    /**
     * Finds all schedules for a specific classroom.
     *
     * @param classroom the classroom
     * @return list of schedules
     */
    List<Schedule> findByClassroom(Classroom classroom);

    /**
     * Finds all schedules for a specific day of week.
     *
     * @param dayOfWeek the day of week
     * @return list of schedules
     */
    List<Schedule> findByDayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Finds all schedules for a specific subject.
     *
     * @param subjectId the subject ID
     * @return list of schedules
     */
    List<Schedule> findBySubjectId(Long subjectId);

    /**
     * Deletes a schedule by ID.
     *
     * @param scheduleId the schedule ID
     */
    void deleteById(Long scheduleId);

    /**
     * Deletes all schedules for a specific group.
     *
     * @param groupId the group ID
     */
    void deleteByGroupId(Long groupId);

    /**
     * Checks if a schedule exists by ID.
     *
     * @param scheduleId the schedule ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long scheduleId);

    /**
     * Counts schedules for a specific group.
     *
     * @param groupId the group ID
     * @return count of schedules
     */
    long countByGroupId(Long groupId);
}
