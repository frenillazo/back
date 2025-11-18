package acainfo.back.session.application.ports.in;

import acainfo.back.session.domain.model.Session;

/**
 * Use case port for cancelling a session.
 * Cancelled sessions are not rescheduled and remain in CANCELADA status.
 */
public interface CancelSessionUseCase {

    /**
     * Cancels an existing session.
     * Validates that the session can be cancelled (must be PROGRAMADA status).
     *
     * @param command the command containing cancellation data
     * @return the cancelled session
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if session cannot be cancelled in current state
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session not found
     */
    Session cancelSession(CancelSessionCommand command);

    /**
     * Command object for cancelling a session
     */
    record CancelSessionCommand(
        Long sessionId,
        String reason // Required reason for cancellation
    ) {}
}
