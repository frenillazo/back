package acainfo.back.session.application.ports.in;

import acainfo.back.session.domain.model.Session;

/**
 * Use case port for creating a new session.
 * Defines the contract for session creation operations.
 */
public interface CreateSessionUseCase {

    /**
     * Creates a new session with the provided data.
     * Validates business rules before creation.
     *
     * @param command the command containing session creation data
     * @return the created session
     * @throws IllegalArgumentException if validation fails
     * @throws acainfo.back.session.domain.exception.SessionConflictException if there are scheduling conflicts
     */
    Session createSession(CreateSessionCommand command);

    /**
     * Command object containing all data needed to create a session
     */
    record CreateSessionCommand(
        Long subjectGroupId,
        String sessionType, // REGULAR, RECUPERACION, EXTRA
        String scheduledStart, // ISO-8601 format
        String scheduledEnd,   // ISO-8601 format
        String mode,          // PRESENCIAL, DUAL, ONLINE
        String classroom,     // AULA_1, AULA_2, VIRTUAL (optional for ONLINE)
        String zoomMeetingId, // Required for ONLINE and DUAL
        String notes,         // Optional notes
        Long generatedFromScheduleId,
        Long recoveryForSessionId, // Optional - if this is a recovery session
        Long originalSessionId     // Optional - if this is a rescheduled session
    ) {}
}
