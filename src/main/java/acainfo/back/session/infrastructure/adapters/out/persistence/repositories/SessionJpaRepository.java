package acainfo.back.session.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository for Session persistence
 * Works with JPA entities only
 */
@Repository
public interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, Long> {

    /**
     * Find all sessions for a subject group
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.id = :groupId ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Find sessions by status
     */
    @Query("SELECT s FROM Session s WHERE s.status = :status ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByStatus(@Param("status") SessionStatus status);

    /**
     * Find sessions by type
     */
    @Query("SELECT s FROM Session s WHERE s.type = :type ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByType(@Param("type") SessionType type);

    /**
     * Find sessions by mode
     */
    @Query("SELECT s FROM Session s WHERE s.mode = :mode ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByMode(@Param("mode") SessionMode mode);

    /**
     * Find sessions by classroom
     */
    @Query("SELECT s FROM Session s WHERE s.classroom = :classroom ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByClassroom(@Param("classroom") Classroom classroom);

    /**
     * Find sessions generated from a specific schedule
     */
    @Query("SELECT s FROM Session s WHERE s.generatedFromSchedule.id = :scheduleId ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByGeneratedFromScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * Find sessions in a date range
     */
    @Query("SELECT s FROM Session s WHERE s.scheduledStart >= :startDate AND s.scheduledEnd <= :endDate ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find sessions for a subject group in a date range
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.id = :groupId " +
           "AND s.scheduledStart >= :startDate AND s.scheduledEnd <= :endDate " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findBySubjectGroupIdAndDateRange(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find sessions by subject group and status
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.id = :groupId AND s.status = :status " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findBySubjectGroupIdAndStatus(
            @Param("groupId") Long groupId,
            @Param("status") SessionStatus status
    );

    /**
     * Find upcoming sessions (scheduled start is in the future)
     */
    @Query("SELECT s FROM Session s WHERE s.scheduledStart > :now AND s.status = :status " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findUpcomingSessions(
            @Param("now") LocalDateTime now,
            @Param("status") SessionStatus status
    );

    /**
     * Find sessions that are happening now
     */
    @Query("SELECT s FROM Session s WHERE s.scheduledStart <= :now AND s.scheduledEnd >= :now " +
           "AND s.status IN :statuses ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findSessionsHappeningNow(
            @Param("now") LocalDateTime now,
            @Param("statuses") List<SessionStatus> statuses
    );

    /**
     * Find sessions that need to be started (scheduled to start soon but not started)
     */
    @Query("SELECT s FROM Session s WHERE s.status = :status " +
           "AND s.scheduledStart <= :threshold AND s.scheduledStart > :now " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findSessionsToStart(
            @Param("status") SessionStatus status,
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold
    );

    /**
     * Find recovery sessions for a specific session
     */
    @Query("SELECT s FROM Session s WHERE s.recoveryForSessionId = :sessionId " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findRecoverySessionsFor(@Param("sessionId") Long sessionId);

    /**
     * Find the original session (when this is a rescheduled session)
     */
    @Query("SELECT s FROM Session s WHERE s.id = :originalSessionId")
    SessionJpaEntity findOriginalSession(@Param("originalSessionId") Long originalSessionId);

    /**
     * Find sessions with classroom conflicts (same classroom, overlapping time, not cancelled)
     */
    @Query("SELECT s FROM Session s WHERE s.classroom = :classroom " +
           "AND s.status NOT IN :excludedStatuses " +
           "AND s.id != :excludeSessionId " +
           "AND ((s.scheduledStart < :endTime AND s.scheduledEnd > :startTime))")
    List<SessionJpaEntity> findClassroomConflicts(
            @Param("classroom") Classroom classroom,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeSessionId") Long excludeSessionId,
            @Param("excludedStatuses") List<SessionStatus> excludedStatuses
    );

    /**
     * Count sessions by subject group and status
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.subjectGroup.id = :groupId AND s.status = :status")
    long countBySubjectGroupIdAndStatus(
            @Param("groupId") Long groupId,
            @Param("status") SessionStatus status
    );

    /**
     * Check if a schedule has generated sessions
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Session s WHERE s.generatedFromSchedule.id = :scheduleId")
    boolean existsByGeneratedFromScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * Delete sessions generated from a specific schedule
     */
    @Query("DELETE FROM Session s WHERE s.generatedFromSchedule.id = :scheduleId " +
           "AND s.status = :status")
    void deleteByGeneratedFromScheduleIdAndStatus(
            @Param("scheduleId") Long scheduleId,
            @Param("status") SessionStatus status
    );

    /**
     * Find sessions by teacher (through subject group relationship)
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.teacherId = :teacherId " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find sessions by teacher in date range
     */
    @Query("SELECT s FROM Session s WHERE s.subjectGroup.teacherId = :teacherId " +
           "AND s.scheduledStart >= :startDate AND s.scheduledEnd <= :endDate " +
           "ORDER BY s.scheduledStart ASC")
    List<SessionJpaEntity> findByTeacherIdAndDateRange(
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
