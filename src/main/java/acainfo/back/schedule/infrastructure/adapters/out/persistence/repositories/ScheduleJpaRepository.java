package acainfo.back.schedule.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * JPA Repository for Schedule persistence
 * Works with ScheduleJpaEntity
 * Provides database operations for schedule management and conflict detection
 */
@Repository
public interface ScheduleJpaRepository extends JpaRepository<ScheduleJpaEntity, Long> {

    /**
     * Finds all schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     * @return list of schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.subjectGroup.id = :groupId ORDER BY s.dayOfWeek, s.startTime")
    List<ScheduleJpaEntity> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Finds schedules by day of week.
     *
     * @param dayOfWeek the day of week
     * @return list of schedules
     */
    List<ScheduleJpaEntity> findByDayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Finds all schedules for a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return list of schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.subjectGroup.teacher.id = :teacherId ORDER BY s.dayOfWeek, s.startTime")
    List<ScheduleJpaEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Finds all schedules for a specific classroom.
     *
     * @param classroom the classroom
     * @return list of schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.classroom = :classroom ORDER BY s.dayOfWeek, s.startTime")
    List<ScheduleJpaEntity> findByClassroom(@Param("classroom") Classroom classroom);

    /**
     * Finds all schedules for a specific subject.
     *
     * @param subjectId the subject ID
     * @return list of schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.subjectGroup.subject.id = :subjectId ORDER BY s.dayOfWeek, s.startTime")
    List<ScheduleJpaEntity> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Finds schedules that conflict with the given time slot on a specific day.
     * A conflict exists if:
     * - Same day of week
     * - Time ranges overlap (startTime < otherEndTime AND otherStartTime < endTime)
     *
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @return list of conflicting schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<ScheduleJpaEntity> findConflictingSchedules(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Finds schedules for a specific teacher that conflict with the given time slot.
     *
     * @param teacherId the teacher ID
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @return list of conflicting schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.subjectGroup.teacher.id = :teacherId " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<ScheduleJpaEntity> findTeacherConflicts(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Finds schedules for a specific classroom that conflict with the given time slot.
     *
     * @param classroom the classroom
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @return list of conflicting schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.classroom = :classroom " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<ScheduleJpaEntity> findClassroomConflicts(
            @Param("classroom") Classroom classroom,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Finds schedules for a specific teacher that conflict with the given time slot,
     * excluding a specific schedule (useful for updates).
     *
     * @param teacherId the teacher ID
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @param excludeScheduleId the schedule ID to exclude
     * @return list of conflicting schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.subjectGroup.teacher.id = :teacherId " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime < :endTime AND s.endTime > :startTime " +
            "AND s.id != :excludeScheduleId")
    List<ScheduleJpaEntity> findTeacherConflictsExcluding(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeScheduleId") Long excludeScheduleId
    );

    /**
     * Finds schedules for a specific classroom that conflict with the given time slot,
     * excluding a specific schedule (useful for updates).
     *
     * @param classroom the classroom
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @param excludeScheduleId the schedule ID to exclude
     * @return list of conflicting schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.classroom = :classroom " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime < :endTime AND s.endTime > :startTime " +
            "AND s.id != :excludeScheduleId")
    List<ScheduleJpaEntity> findClassroomConflictsExcluding(
            @Param("classroom") Classroom classroom,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeScheduleId") Long excludeScheduleId
    );

    /**
     * Finds schedules within a specific time range on a given day.
     *
     * @param dayOfWeek the day of week
     * @param startTime the start time
     * @param endTime the end time
     * @return list of schedules
     */
    @Query("SELECT s FROM Schedule s WHERE s.dayOfWeek = :dayOfWeek " +
            "AND ((s.startTime >= :startTime AND s.startTime < :endTime) " +
            "OR (s.endTime > :startTime AND s.endTime <= :endTime) " +
            "OR (s.startTime <= :startTime AND s.endTime >= :endTime))")
    List<ScheduleJpaEntity> findByDayOfWeekAndTimeRange(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Counts schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     * @return count of schedules
     */
    long countBySubjectGroupId(Long groupId);

    /**
     * Counts schedules for a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return count of schedules
     */
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.subjectGroup.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Deletes all schedules for a specific subjectGroup.
     *
     * @param groupId the subjectGroup ID
     */
    void deleteBySubjectGroupId(Long groupId);
}
