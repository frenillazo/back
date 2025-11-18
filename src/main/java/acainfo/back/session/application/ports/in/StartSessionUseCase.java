package acainfo.back.session.application.ports.in;

import acainfo.back.session.domain.model.Session;

/**
 * Use case port for starting a session.
 * Starting a session transitions it from PROGRAMADA to EN_CURSO status.
 */
public interface StartSessionUseCase {

    /**
     * Starts an existing session.
     * Validates that the session can be started (must be PROGRAMADA status
     * and within 30 minutes of scheduled start time).
     *
     * @param command the command containing session start data
     * @return the started session
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if session cannot be started in current state
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session not found
     */
    Session startSession(StartSessionCommand command);

    /**
     * Command object for starting a session
     */
    record StartSessionCommand(
        Long sessionId,
        String notes // Optional notes when starting the session
    ) {}
}
