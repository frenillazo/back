package com.acainfo.session.application.port.in;

import com.acainfo.session.application.dto.CreateSessionCommand;
import com.acainfo.session.domain.model.Session;

/**
 * Use case for creating sessions.
 * Input port defining the contract for session creation.
 */
public interface CreateSessionUseCase {

    /**
     * Create a new session.
     *
     * @param command Session creation data
     * @return The created session
     * @throws com.acainfo.session.domain.exception.SessionConflictException if there's a scheduling conflict
     */
    Session create(CreateSessionCommand command);
}
