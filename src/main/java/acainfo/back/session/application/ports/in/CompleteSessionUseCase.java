package acainfo.back.session.application.ports.in;

import acainfo.back.session.domain.model.Session;

/**
 * Use case port for completing a session.
 * Completing a session marks it as COMPLETADA and records the topics covered.
 */
public interface CompleteSessionUseCase {

    /**
     * Completes an existing session.
     * Validates that the session can be completed (must be EN_CURSO status).
     *
     * @param command the command containing completion data
     * @return the completed session
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if session cannot be completed in current state
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session not found
     */
    Session completeSession(CompleteSessionCommand command);

    /**
     * Command object for completing a session
     */
    record CompleteSessionCommand(
        Long sessionId,
        String topicsCovered, // Required - what was taught in the session
        String notes          // Optional additional notes
    ) {}
}
