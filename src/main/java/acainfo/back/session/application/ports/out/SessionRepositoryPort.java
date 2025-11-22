package acainfo.back.session.application.ports.out;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Port for Session
 * Application layer - defines repository contract using domain models
 */
public interface SessionRepositoryPort {

    // ==================== CRUD OPERATIONS ====================

    SessionDomain save(SessionDomain session);

    Optional<SessionDomain> findById(Long id);

    List<SessionDomain> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    // ==================== BASIC QUERIES ====================

    List<SessionDomain> findBySubjectGroupId(Long groupId);

    List<SessionDomain> findBySubjectGroupIdAndStatus(Long groupId, SessionStatus status);

    List<SessionDomain> findBySubjectGroupIdAndDateRange(
            Long groupId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<SessionDomain> findByStatus(SessionStatus status);

    List<SessionDomain> findByType(SessionType type);

    List<SessionDomain> findByMode(SessionMode mode);

    // ==================== TEACHER QUERIES ====================

    List<SessionDomain> findByTeacherId(Long teacherId);

    List<SessionDomain> findByTeacherIdAndDateRange(
            Long teacherId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<SessionDomain> findActiveSessionsByTeacherId(Long teacherId);

    // ==================== CLASSROOM QUERIES ====================

    List<SessionDomain> findByClassroom(Classroom classroom);

    List<SessionDomain> findByClassroomAndDateRange(
            Classroom classroom,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<SessionDomain> findConflictingClassroomSessions(
            Classroom classroom,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeSessionId
    );

    boolean hasClassroomConflict(
            Classroom classroom,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeSessionId
    );

    // ==================== TIME-BASED QUERIES ====================

    List<SessionDomain> findUpcomingSessions(LocalDateTime fromDate);

    List<SessionDomain> findSessionsStartingInNext24Hours(
            LocalDateTime now,
            LocalDateTime tomorrow
    );

    List<SessionDomain> findInProgressSessions();

    List<SessionDomain> findLateSessions(LocalDateTime now);

    List<SessionDomain> findByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // ==================== RECOVERY AND POSTPONEMENT QUERIES ====================

    List<SessionDomain> findSessionsRequiringAction();

    Optional<SessionDomain> findRecoverySession(Long originalSessionId);

    List<SessionDomain> findRecoverySessionsFor(Long sessionId);

    List<SessionDomain> findRecoverySessionsByGroupId(Long groupId);

    List<SessionDomain> findSessionsWithRecentChanges(LocalDateTime since);

    // ==================== VALIDATION QUERIES ====================

    boolean hasTeacherConflict(
            Long teacherId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeSessionId
    );

    // ==================== COUNTING QUERIES ====================

    long countBySubjectGroupIdAndStatus(Long groupId, SessionStatus status);

    long countBySubjectGroupId(Long groupId);

    // ==================== STATUS-BASED QUERIES ====================

    List<SessionDomain> findCompletedSessionsInDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<SessionDomain> findCancelledSessionsByGroupId(Long groupId);

    // ==================== STATISTICS QUERIES ====================

    List<Object[]> countSessionsByStatusForTeacher(Long teacherId);

    List<Object[]> countSessionsByStatusForGroup(Long groupId);

    Double getCompletionRateForGroup(Long groupId);

    // ==================== SCHEDULE RELATIONSHIP QUERIES ====================

    List<SessionDomain> findByGeneratedFromScheduleId(Long scheduleId);

    List<SessionDomain> findByGeneratedFromScheduleIdAndDateRange(
            Long scheduleId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<SessionDomain> findManuallyCreatedSessions();

    List<SessionDomain> findManuallyCreatedSessionsByGroupId(Long groupId);

    long countByGeneratedFromScheduleId(Long scheduleId);

    long countByGeneratedFromScheduleIdAndStatus(Long scheduleId, SessionStatus status);

    boolean existsByGeneratedFromScheduleId(Long scheduleId);

    Double getCompletionRateForSchedule(Long scheduleId);

    Optional<SessionDomain> findMostRecentSessionByScheduleId(Long scheduleId);

    Optional<SessionDomain> findNextUpcomingSessionByScheduleId(Long scheduleId, LocalDateTime now);

    void deleteByGeneratedFromScheduleIdAndStatus(Long scheduleId, SessionStatus status);
}
