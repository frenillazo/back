package acainfo.back.session.application.usecases;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.application.ports.in.CreateSessionUseCase;
import acainfo.back.session.application.ports.in.PostponeSessionUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of PostponeSessionUseCase
 * Handles postponing sessions (PROGRAMADA → POSPUESTA)
 * and optionally creating recovery sessions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostponeSessionUseCaseImpl implements PostponeSessionUseCase {

    private final SessionRepositoryPort repository;
    private final CreateSessionUseCase createSessionUseCase;

    @Override
    public SessionDomain postponeSession(PostponeSessionCommand command) {
        log.info("Postponing session {}", command.sessionId());

        // 1. Validate reason
        if (command.reason() == null || command.reason().isBlank()) {
            throw new IllegalArgumentException("Reason is required to postpone a session");
        }

        // 2. Fetch session
        SessionDomain session = repository.findById(command.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 3. Postpone session (domain method validates state)
        SessionDomain postponedSession = session.postpone(command.reason());

        // 4. Save
        SessionDomain saved = repository.save(postponedSession);

        log.info("Session {} postponed. Reason: {}", saved.getId(), command.reason());
        return saved;
    }

    @Override
    public SessionDomain postponeAndReschedule(PostponeAndRescheduleCommand command) {
        log.info("Postponing session {} and creating recovery session", command.sessionId());

        // 1. Postpone original session
        SessionDomain postponedSession = postponeSession(
                new PostponeSessionCommand(command.sessionId(), command.reason())
        );

        // 2. Parse new dates
        LocalDateTime newStart = LocalDateTime.parse(command.newScheduledStart());
        LocalDateTime newEnd = LocalDateTime.parse(command.newScheduledEnd());

        // 3. Determine classroom and zoom (use original if not provided)
        String classroom = command.classroom() != null ?
                command.classroom() :
                (postponedSession.getClassroom() != null ? postponedSession.getClassroom().name() : null);

        String zoomMeetingId = command.zoomMeetingId() != null ?
                command.zoomMeetingId() :
                postponedSession.getZoomMeetingId();

        // 4. Create recovery session
        SessionDomain recoverySession = createSessionUseCase.createSession(
                new CreateSessionUseCase.CreateSessionCommand(
                        postponedSession.getSubjectGroupId(),
                        SessionType.RECUPERACION.name(),
                        command.newScheduledStart(),
                        command.newScheduledEnd(),
                        postponedSession.getMode().name(),
                        classroom,
                        zoomMeetingId,
                        "Recovery session for postponed session #" + postponedSession.getId(),
                        null, // No schedule origin for recovery sessions
                        postponedSession.getId(), // This recovers the postponed session
                        null
                )
        );

        log.info("Recovery session {} created for postponed session {}",
                recoverySession.getId(), postponedSession.getId());

        return recoverySession;
    }
}
