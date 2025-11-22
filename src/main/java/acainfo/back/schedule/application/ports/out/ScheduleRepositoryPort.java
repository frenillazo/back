package acainfo.back.schedule.application.ports.out;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for Schedule repository operations.
 * Works with ScheduleDomain (pure domain model)
 */
public interface ScheduleRepositoryPort {

    /**
     * Saves a schedule.
     *
     * @param schedule the schedule to save
     * @return the saved schedule
     */
    ScheduleDomain save(ScheduleDomain schedule);

    /**
     * Finds a schedule by ID.
     *
     * @param scheduleId the schedule ID
     * @return Optional containing the schedule if found
     */
    Optional<ScheduleDomain> findById(Long scheduleId);

    /**
     * Finds all schedules.
     *
     * @return list of all schedules
     */
    List<ScheduleDomain> findAll();

    /**
     * Finds all schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     * @return list of schedules
     */
    List<ScheduleDomain> findByGroupId(Long groupId);

    /**
     * Finds all schedules for a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return list of schedules
     */
    List<ScheduleDomain> findByTeacherId(Long teacherId);

    /**
     * Finds all schedules for a specific classroom.
     *
     * @param classroom the classroom
     * @return list of schedules
     */
    List<ScheduleDomain> findByClassroom(Classroom classroom);

    /**
     * Finds all schedules for a specific day of week.
     *
     * @param dayOfWeek the day of week
     * @return list of schedules
     */
    List<ScheduleDomain> findByDayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Finds all schedules for a specific subject.
     *
     * @param subjectId the subject ID
     * @return list of schedules
     */
    List<ScheduleDomain> findBySubjectId(Long subjectId);

    /**
     * Deletes a schedule by ID.
     *
     * @param scheduleId the schedule ID
     */
    void deleteById(Long scheduleId);

    /**
     * Deletes all schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
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
     * Counts schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     * @return count of schedules
     */
    long countByGroupId(Long groupId);
}
