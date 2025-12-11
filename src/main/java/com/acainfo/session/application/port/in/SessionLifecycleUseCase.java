package com.acainfo.session.application.port.in;

import com.acainfo.session.application.dto.PostponeSessionCommand;
import com.acainfo.session.domain.model.Session;

/**
 * Use case for managing session lifecycle transitions.
 * Input port defining the contract for session state changes.
 *
 * <p>State transitions:</p>
 * <pre>
 * SCHEDULED ──┬──► IN_PROGRESS ──► COMPLETED
 *             │
 *             ├──► CANCELLED
 *             │
 *             └──► POSTPONED ──► (creates new SCHEDULED session)
 * </pre>
 */
public interface SessionLifecycleUseCase {

    /**
     * Start a session (transition from SCHEDULED to IN_PROGRESS).
     *
     * @param id Session ID
     * @return The updated session
     * @throws com.acainfo.session.domain.exception.InvalidSessionStateException if not in SCHEDULED state
     */
    Session start(Long id);

    /**
     * Complete a session (transition from IN_PROGRESS to COMPLETED).
     *
     * @param id Session ID
     * @return The updated session
     * @throws com.acainfo.session.domain.exception.InvalidSessionStateException if not in IN_PROGRESS state
     */
    Session complete(Long id);

    /**
     * Cancel a session (transition from SCHEDULED to CANCELLED).
     *
     * @param id Session ID
     * @return The updated session
     * @throws com.acainfo.session.domain.exception.InvalidSessionStateException if not in SCHEDULED state
     */
    Session cancel(Long id);

    /**
     * Postpone a session to a new date/time.
     * The original session is marked as POSTPONED and a new SCHEDULED session is created.
     *
     * @param id Session ID
     * @param command Postpone details (new date, time, etc.)
     * @return The newly created session with the new date
     * @throws com.acainfo.session.domain.exception.InvalidSessionStateException if not in SCHEDULED state
     * @throws com.acainfo.session.domain.exception.SessionConflictException if new date/time conflicts
     */
    Session postpone(Long id, PostponeSessionCommand command);
}
