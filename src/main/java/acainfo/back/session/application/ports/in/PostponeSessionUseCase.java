package acainfo.back.session.application.ports.in;

import acainfo.back.session.domain.model.Session;

/**
 * Use case port for postponing a session.
 * When a session is postponed, it is marked as POSPUESTA and a new session
 * should be created to replace it.
 */
public interface PostponeSessionUseCase {

    /**
     * Postpones an existing session and optionally creates a recovery session.
     * Validates that the session can be postponed (at least 2 hours before start).
     *
     * @param command the command containing postponement data
     * @return the postponed session
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if session cannot be postponed in current state
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session not found
     */
    Session postponeSession(PostponeSessionCommand command);

    /**
     * Postpones a session and creates a new recovery session in one operation.
     *
     * @param command the command containing postponement and rescheduling data
     * @return the newly created recovery session
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if session cannot be postponed
     * @throws acainfo.back.session.domain.exception.SessionConflictException if new time slot has conflicts
     */
    Session postponeAndReschedule(PostponeAndRescheduleCommand command);

    /**
     * Command object for postponing a session
     */
    record PostponeSessionCommand(
        Long sessionId,
        String reason // Required reason for postponement
    ) {}

    /**
     * Command object for postponing and creating a recovery session
     */
    record PostponeAndRescheduleCommand(
        Long sessionId,
        String reason,
        String newScheduledStart, // ISO-8601 format for the recovery session
        String newScheduledEnd,   // ISO-8601 format for the recovery session
        String classroom,         // Optional - defaults to original if not provided
        String zoomMeetingId      // Optional - defaults to original if not provided
    ) {}
}
