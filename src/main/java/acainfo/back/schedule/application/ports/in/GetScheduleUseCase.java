package acainfo.back.schedule.application.ports.in;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Use case interface for retrieving schedule information.
 * Works with ScheduleDomain (pure domain model)
 */
public interface GetScheduleUseCase {

    /**
     * Gets a schedule by ID.
     *
     * @param scheduleId the schedule ID
     * @return the schedule
     */
    ScheduleDomain getScheduleById(Long scheduleId);

    /**
     * Gets all schedules.
     *
     * @return list of all schedules
     */
    List<ScheduleDomain> getAllSchedules();

    /**
     * Gets all schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     * @return list of schedules
     */
    List<ScheduleDomain> getSchedulesByGroupId(Long groupId);

    /**
     * Gets all schedules for a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return list of schedules
     */
    List<ScheduleDomain> getSchedulesByTeacherId(Long teacherId);

    /**
     * Gets all schedules for a specific classroom.
     *
     * @param classroom the classroom
     * @return list of schedules
     */
    List<ScheduleDomain> getSchedulesByClassroom(Classroom classroom);

    /**
     * Gets all schedules for a specific day of week.
     *
     * @param dayOfWeek the day of week
     * @return list of schedules
     */
    List<ScheduleDomain> getSchedulesByDayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Gets all schedules for a specific subject.
     *
     * @param subjectId the subject ID
     * @return list of schedules
     */
    List<ScheduleDomain> getSchedulesBySubjectId(Long subjectId);
}
