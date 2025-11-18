package acainfo.back.session.application.ports.in;

import acainfo.back.session.domain.model.Session;

/**
 * Use case port for updating session mode (PRESENCIAL, DUAL, ONLINE).
 * Allows teachers to change the delivery mode of a session.
 */
public interface UpdateSessionModeUseCase {

    /**
     * Updates the mode of an existing session.
     * Validates that the change is allowed (e.g., not less than 2 hours before start).
     *
     * @param command the command containing mode update data
     * @return the updated session
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if mode cannot be changed in current state
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session not found
     */
    Session updateSessionMode(UpdateSessionModeCommand command);

    /**
     * Command object containing data needed to update session mode
     */
    record UpdateSessionModeCommand(
        Long sessionId,
        String newMode,       // PRESENCIAL, DUAL, ONLINE
        String classroom,     // Required if changing to PRESENCIAL or DUAL
        String zoomMeetingId, // Required if changing to ONLINE or DUAL
        String reason         // Optional reason for the change
    ) {}
}
