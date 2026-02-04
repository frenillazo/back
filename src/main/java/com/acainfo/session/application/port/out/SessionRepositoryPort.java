package com.acainfo.session.application.port.out;

import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.domain.model.Session;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port for Session persistence.
 * Defines the contract for Session repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface SessionRepositoryPort {

    /**
     * Save or update a session.
     *
     * @param session Domain session to persist
     * @return Persisted session with ID
     */
    Session save(Session session);

    /**
     * Save multiple sessions in batch.
     *
     * @param sessions List of domain sessions to persist
     * @return List of persisted sessions with IDs
     */
    List<Session> saveAll(List<Session> sessions);

    /**
     * Find session by ID.
     *
     * @param id Session ID
     * @return Optional containing the session if found
     */
    Optional<Session> findById(Long id);

    /**
     * Find sessions with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of sessions matching filters
     */
    Page<Session> findWithFilters(SessionFilters filters);

    /**
     * Find all sessions for a specific group.
     *
     * @param groupId Group ID
     * @return List of sessions for the group
     */
    List<Session> findByGroupId(Long groupId);

    /**
     * Find all sessions for a specific subject.
     *
     * @param subjectId Subject ID
     * @return List of sessions for the subject
     */
    List<Session> findBySubjectId(Long subjectId);

    /**
     * Find all sessions generated from a specific schedule.
     *
     * @param scheduleId Schedule ID
     * @return List of sessions for the schedule
     */
    List<Session> findByScheduleId(Long scheduleId);

    /**
     * Check if a session already exists for a schedule on a specific date.
     * Used to prevent duplicate session generation.
     *
     * @param scheduleId Schedule ID
     * @param date Session date
     * @return true if session exists
     */
    boolean existsByScheduleIdAndDate(Long scheduleId, LocalDate date);

    /**
     * Delete a session by ID.
     *
     * @param id Session ID
     */
    void delete(Long id);

    /**
     * Check if there are any sessions for a group on a specific date and time range.
     * Used for conflict detection.
     *
     * @param groupId Group ID
     * @param date Session date
     * @param excludeSessionId Session ID to exclude from check (for updates)
     * @return true if there's a conflicting session
     */
    boolean existsConflictingSession(Long groupId, LocalDate date, Long excludeSessionId);

    /**
     * Find upcoming scheduled sessions for multiple groups.
     * Used for student dashboard to show next sessions across all enrolled groups.
     *
     * @param groupIds List of group IDs
     * @param fromDate Start date (inclusive)
     * @param limit Maximum number of sessions to return
     * @return List of upcoming sessions sorted by date ascending
     */
    List<Session> findUpcomingByGroupIds(List<Long> groupIds, LocalDate fromDate, int limit);

    /**
     * Find sessions by teacher ID and date.
     * Used for teacher conflict validation when creating sessions.
     * Includes all session types (REGULAR, EXTRA, SCHEDULING) that are not cancelled.
     *
     * @param teacherId Teacher ID
     * @param date Session date
     * @return List of sessions for the teacher on that date
     */
    List<Session> findByTeacherIdAndDate(Long teacherId, LocalDate date);
}
