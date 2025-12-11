package com.acainfo.session.application.port.in;

import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.domain.model.Session;

import java.util.List;

/**
 * Use case for generating sessions from schedules.
 * Input port defining the contract for bulk session generation.
 *
 * <p>This use case is used to automatically create REGULAR sessions
 * based on the defined schedules for groups within a date range.</p>
 */
public interface GenerateSessionsUseCase {

    /**
     * Generate sessions from schedules for the specified period.
     *
     * @param command Generation parameters (group, date range)
     * @return List of generated sessions
     */
    List<Session> generate(GenerateSessionsCommand command);

    /**
     * Preview sessions that would be generated without actually creating them.
     * Useful for validation before bulk generation.
     *
     * @param command Generation parameters (group, date range)
     * @return List of sessions that would be generated
     */
    List<Session> preview(GenerateSessionsCommand command);
}
