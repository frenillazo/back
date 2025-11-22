package acainfo.back.session.application.ports.in;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use case port for querying sessions.
 * Read-only operations for retrieving session data.
 */
public interface GetSessionUseCase {

    /**
     * Find a session by ID
     */
    Optional<SessionDomain> findById(Long id);

    /**
     * Find all sessions
     */
    List<SessionDomain> findAll();

    /**
     * Find sessions by subject group ID
     */
    List<SessionDomain> findBySubjectGroupId(Long groupId);

    /**
     * Find sessions by subject group and status
     */
    List<SessionDomain> findBySubjectGroupIdAndStatus(Long groupId, SessionStatus status);

    /**
     * Find sessions by subject group in date range
     */
    List<SessionDomain> findBySubjectGroupIdAndDateRange(
            Long groupId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find sessions by teacher ID
     */
    List<SessionDomain> findByTeacherId(Long teacherId);

    /**
     * Find sessions by teacher in date range
     */
    List<SessionDomain> findByTeacherIdAndDateRange(
            Long teacherId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find active sessions for a teacher
     */
    List<SessionDomain> findActiveSessionsByTeacherId(Long teacherId);

    /**
     * Find sessions by classroom
     */
    List<SessionDomain> findByClassroom(Classroom classroom);

    /**
     * Find sessions by status
     */
    List<SessionDomain> findByStatus(SessionStatus status);

    /**
     * Find sessions by type
     */
    List<SessionDomain> findByType(SessionType type);

    /**
     * Find sessions by mode
     */
    List<SessionDomain> findByMode(SessionMode mode);

    /**
     * Find upcoming sessions
     */
    List<SessionDomain> findUpcomingSessions(LocalDateTime fromDate);

    /**
     * Find sessions starting in next 24 hours
     */
    List<SessionDomain> findSessionsStartingInNext24Hours();

    /**
     * Find sessions currently in progress
     */
    List<SessionDomain> findInProgressSessions();

    /**
     * Find late sessions (should have started but haven't)
     */
    List<SessionDomain> findLateSessions();

    /**
     * Find sessions in date range
     */
    List<SessionDomain> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find sessions generated from a schedule
     */
    List<SessionDomain> findByGeneratedFromScheduleId(Long scheduleId);

    /**
     * Find manually created sessions (not generated from schedule)
     */
    List<SessionDomain> findManuallyCreatedSessions();

    /**
     * Find recovery sessions for a specific session
     */
    List<SessionDomain> findRecoverySessionsFor(Long sessionId);

    /**
     * Find sessions requiring action (postponed without recovery)
     */
    List<SessionDomain> findSessionsRequiringAction();

    /**
     * Get completion rate for a group
     */
    Double getCompletionRateForGroup(Long groupId);

    /**
     * Get completion rate for a schedule
     */
    Double getCompletionRateForSchedule(Long scheduleId);

    /**
     * Count sessions by subject group
     */
    long countBySubjectGroupId(Long groupId);

    /**
     * Count sessions by subject group and status
     */
    long countBySubjectGroupIdAndStatus(Long groupId, SessionStatus status);
}
