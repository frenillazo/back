package com.acainfo.session.application.port.in;

import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.domain.model.Session;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Use case for retrieving sessions.
 * Input port defining the contract for session queries.
 */
public interface GetSessionUseCase {

    /**
     * Get a session by ID.
     *
     * @param id Session ID
     * @return The session
     * @throws com.acainfo.session.domain.exception.SessionNotFoundException if not found
     */
    Session getById(Long id);

    /**
     * Find sessions with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of sessions matching the filters
     */
    Page<Session> findWithFilters(SessionFilters filters);

    /**
     * Get all sessions for a specific group.
     *
     * @param groupId Group ID
     * @return List of sessions for the group
     */
    List<Session> findByGroupId(Long groupId);

    /**
     * Get all sessions for a specific subject.
     *
     * @param subjectId Subject ID
     * @return List of sessions for the subject
     */
    List<Session> findBySubjectId(Long subjectId);

    /**
     * Get all sessions generated from a specific schedule.
     *
     * @param scheduleId Schedule ID
     * @return List of sessions for the schedule
     */
    List<Session> findByScheduleId(Long scheduleId);
}
