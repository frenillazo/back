package acainfo.back.session.infrastructure.adapters.out;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.Schedule;
import acainfo.back.session.domain.model.Session;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Session entity.
 * Provides comprehensive query methods for session management.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // ==================== BASIC QUERIES ====================

    /**
     * Find all sessions for a specific subject group
     */
    List<Session> findBySubjectGroup(SubjectGroup subjectGroup);

    /**
     * Find all sessions for a subject group with specific status
     */
    List<Session> findBySubjectGroupAndStatus(SubjectGroup subjectGroup, SessionStatus status);

    /**
     * Find sessions for a subject group within a date range
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup = :subjectGroup " +
           "AND s.scheduledStart >= :startDate AND s.scheduledStart <= :endDate " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findBySubjectGroupAndDateRange(
        @Param("subjectGroup") SubjectGroup subjectGroup,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all sessions by subject group ID
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.id = :groupId ORDER BY s.scheduledStart ASC")
    List<Session> findBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Find sessions by subject group ID within a date range
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.id = :groupId " +
           "AND s.scheduledStart >= :startDate AND s.scheduledStart <= :endDate " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findBySubjectGroupIdAndScheduledStartBetween(
        @Param("groupId") Long groupId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ==================== TEACHER QUERIES ====================

    /**
     * Find all sessions for a specific teacher
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.teacher.id = :teacherId ORDER BY s.scheduledStart ASC")
    List<Session> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find sessions for a teacher within a date range
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.teacher.id = :teacherId " +
           "AND s.scheduledStart >= :startDate AND s.scheduledStart <= :endDate " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findByTeacherIdAndDateRange(
        @Param("teacherId") Long teacherId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find active sessions for a teacher (PROGRAMADA or EN_CURSO)
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.teacher.id = :teacherId " +
           "AND s.status IN ('PROGRAMADA', 'EN_CURSO') " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findActiveSessionsByTeacherId(@Param("teacherId") Long teacherId);

    // ==================== CLASSROOM QUERIES ====================

    /**
     * Find sessions for a specific classroom within a date range
     */
    @Query("SELECT s FROM Session s WHERE s.classroom = :classroom " +
           "AND s.scheduledStart >= :startDate AND s.scheduledStart <= :endDate " +
           "AND s.status NOT IN ('CANCELADA', 'POSPUESTA') " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findByClassroomAndDateRange(
        @Param("classroom") Classroom classroom,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find sessions that conflict with a given time slot in a classroom
     * (excluding a specific session by ID, useful for updates)
     */
    @Query("SELECT s FROM Session s WHERE s.classroom = :classroom " +
           "AND s.id != :excludeSessionId " +
           "AND s.status NOT IN ('CANCELADA', 'POSPUESTA') " +
           "AND ((s.scheduledStart < :endTime AND s.scheduledEnd > :startTime))")
    List<Session> findConflictingClassroomSessions(
        @Param("classroom") Classroom classroom,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeSessionId") Long excludeSessionId
    );

    // ==================== TIME-BASED QUERIES ====================

    /**
     * Find upcoming sessions from a specific date
     */
    @Query("SELECT s FROM Session s WHERE s.scheduledStart >= :fromDate " +
           "AND s.status = 'PROGRAMADA' " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findUpcomingSessions(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find sessions starting in the next 24 hours
     */
    @Query("SELECT s FROM Session s WHERE s.scheduledStart >= :now " +
           "AND s.scheduledStart <= :tomorrow " +
           "AND s.status = 'PROGRAMADA' " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findSessionsStartingInNext24Hours(
        @Param("now") LocalDateTime now,
        @Param("tomorrow") LocalDateTime tomorrow
    );

    /**
     * Find sessions that are currently in progress
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'EN_CURSO' ORDER BY s.scheduledStart ASC")
    List<Session> findInProgressSessions();

    /**
     * Find sessions that should have started but haven't (late sessions)
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'PROGRAMADA' " +
           "AND s.scheduledStart < :now " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findLateSessions(@Param("now") LocalDateTime now);

    // ==================== RECOVERY AND POSTPONEMENT QUERIES ====================

    /**
     * Find sessions requiring action (postponed without recovery session)
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'POSPUESTA' " +
           "AND NOT EXISTS (SELECT r FROM Session r WHERE r.recoveryForSessionId = s.id) " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findSessionsRequiringAction();

    /**
     * Find the recovery session for a given session
     */
    @Query("SELECT s FROM Session s WHERE s.recoveryForSessionId = :originalSessionId")
    Optional<Session> findRecoverySession(@Param("originalSessionId") Long originalSessionId);

    /**
     * Find all recovery sessions for a subject group
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup = :subjectGroup " +
           "AND s.type = 'RECUPERACION' " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findRecoverySessionsByGroup(@Param("subjectGroup") SubjectGroup subjectGroup);

    /**
     * Find sessions with mode changes in the last N hours
     */
    @Query("SELECT s FROM Session s WHERE s.updatedAt >= :since " +
           "AND s.status = 'PROGRAMADA' " +
           "ORDER BY s.updatedAt DESC")
    List<Session> findSessionsWithRecentChanges(@Param("since") LocalDateTime since);

    // ==================== VALIDATION QUERIES ====================

    /**
     * Check if a classroom is available for a given time slot
     * (excluding a specific session, useful for updates)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Session s " +
           "WHERE s.classroom = :classroom " +
           "AND s.id != :excludeSessionId " +
           "AND s.status NOT IN ('CANCELADA', 'POSPUESTA') " +
           "AND ((s.scheduledStart < :endTime AND s.scheduledEnd > :startTime))")
    boolean existsByClassroomAndDateRangeExcludingId(
        @Param("classroom") Classroom classroom,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeSessionId") Long excludeSessionId
    );

    /**
     * Check if a teacher has conflicting sessions
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Session s " +
           "WHERE s.subjectGroup.teacher.id = :teacherId " +
           "AND s.id != :excludeSessionId " +
           "AND s.status NOT IN ('CANCELADA', 'POSPUESTA') " +
           "AND ((s.scheduledStart < :endTime AND s.scheduledEnd > :startTime))")
    boolean existsByTeacherAndDateRangeExcludingId(
        @Param("teacherId") Long teacherId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeSessionId") Long excludeSessionId
    );

    /**
     * Count sessions by subject group and status
     */
    long countBySubjectGroupAndStatus(SubjectGroup subjectGroup, SessionStatus status);

    /**
     * Count total sessions for a subject group
     */
    long countBySubjectGroup(SubjectGroup subjectGroup);

    // ==================== STATUS-BASED QUERIES ====================

    /**
     * Find all sessions with a specific status
     */
    List<Session> findByStatus(SessionStatus status);

    /**
     * Find completed sessions within a date range
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'COMPLETADA' " +
           "AND s.actualEnd >= :startDate AND s.actualEnd <= :endDate " +
           "ORDER BY s.actualEnd DESC")
    List<Session> findCompletedSessionsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find cancelled sessions for a subject group
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup = :subjectGroup " +
           "AND s.status = 'CANCELADA' " +
           "ORDER BY s.scheduledStart DESC")
    List<Session> findCancelledSessionsByGroup(@Param("subjectGroup") SubjectGroup subjectGroup);

    // ==================== STATISTICS QUERIES ====================

    /**
     * Count sessions by status for a teacher
     */
    @Query("SELECT s.status, COUNT(s) FROM Session s " +
           "WHERE s.subjectGroup.teacher.id = :teacherId " +
           "GROUP BY s.status")
    List<Object[]> countSessionsByStatusForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Count sessions by status for a subject group
     */
    @Query("SELECT s.status, COUNT(s) FROM Session s " +
           "WHERE s.subjectGroup = :subjectGroup " +
           "GROUP BY s.status")
    List<Object[]> countSessionsByStatusForGroup(@Param("subjectGroup") SubjectGroup subjectGroup);

    /**
     * Get completion rate for a subject group
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN s.status = 'COMPLETADA' THEN 1 END) * 100.0 / COUNT(s) " +
           "FROM Session s WHERE s.subjectGroup = :subjectGroup")
    Double getCompletionRateForGroup(@Param("subjectGroup") SubjectGroup subjectGroup);

    // ==================== SCHEDULE RELATIONSHIP QUERIES ====================

    /**
     * Find all sessions generated from a specific schedule
     */
    @Query("SELECT s FROM Session s WHERE s.generatedFromSchedule = :schedule " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findByGeneratedFromSchedule(@Param("schedule") Schedule schedule);

    /**
     * Find sessions generated from a schedule within a date range
     */
    @Query("SELECT s FROM Session s WHERE s.generatedFromSchedule = :schedule " +
           "AND s.scheduledStart >= :startDate AND s.scheduledStart <= :endDate " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findByGeneratedFromScheduleAndDateRange(
        @Param("schedule") Schedule schedule,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find sessions NOT generated from any schedule (manually created)
     */
    @Query("SELECT s FROM Session s WHERE s.generatedFromSchedule IS NULL " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findManuallyCreatedSessions();

    /**
     * Find manually created sessions for a subject group
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup = :subjectGroup " +
           "AND s.generatedFromSchedule IS NULL " +
           "ORDER BY s.scheduledStart ASC")
    List<Session> findManuallyCreatedSessionsByGroup(@Param("subjectGroup") SubjectGroup subjectGroup);

    /**
     * Count sessions generated from a specific schedule
     */
    long countByGeneratedFromSchedule(Schedule schedule);

    /**
     * Count sessions by schedule and status
     */
    long countByGeneratedFromScheduleAndStatus(Schedule schedule, SessionStatus status);

    /**
     * Check if a schedule has any generated sessions
     */
    boolean existsByGeneratedFromSchedule(Schedule schedule);

    /**
     * Find all schedules that have generated sessions
     */
    @Query("SELECT DISTINCT s.generatedFromSchedule FROM Session s " +
           "WHERE s.generatedFromSchedule IS NOT NULL")
    List<Schedule> findDistinctSchedulesWithGeneratedSessions();

    /**
     * Get completion rate for a specific schedule
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN s.status = 'COMPLETADA' THEN 1 END) * 100.0 / COUNT(s) " +
           "FROM Session s WHERE s.generatedFromSchedule = :schedule")
    Double getCompletionRateForSchedule(@Param("schedule") Schedule schedule);

    /**
     * Find the most recent session generated from a schedule
     */
    @Query("SELECT s FROM Session s WHERE s.generatedFromSchedule = :schedule " +
           "ORDER BY s.scheduledStart DESC LIMIT 1")
    Optional<Session> findMostRecentSessionBySchedule(@Param("schedule") Schedule schedule);

    /**
     * Find the next upcoming session from a schedule
     */
    @Query("SELECT s FROM Session s WHERE s.generatedFromSchedule = :schedule " +
           "AND s.scheduledStart > :now AND s.status = 'PROGRAMADA' " +
           "ORDER BY s.scheduledStart ASC LIMIT 1")
    Optional<Session> findNextUpcomingSessionBySchedule(
        @Param("schedule") Schedule schedule,
        @Param("now") LocalDateTime now
    );
}
