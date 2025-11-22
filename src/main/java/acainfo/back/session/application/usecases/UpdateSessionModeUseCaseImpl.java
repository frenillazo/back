package acainfo.back.session.application.usecases;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.application.ports.in.UpdateSessionModeUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UpdateSessionModeUseCase
 * Handles changing session mode (PRESENCIAL ↔ DUAL ↔ ONLINE)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateSessionModeUseCaseImpl implements UpdateSessionModeUseCase {

    private final SessionRepositoryPort repository;

    @Override
    public SessionDomain updateSessionMode(UpdateSessionModeCommand command) {
        log.info("Updating mode for session {} to {}", command.sessionId(), command.newMode());

        // 1. Fetch session
        SessionDomain session = repository.findById(command.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 2. Parse new mode and classroom
        SessionMode newMode = SessionMode.valueOf(command.newMode());
        Classroom classroom = command.classroom() != null ?
                Classroom.valueOf(command.classroom()) : session.getClassroom();

        // 3. Store old mode for logging
        SessionMode oldMode = session.getMode();

        // 4. Change mode (domain method validates)
        SessionDomain updatedSession = session.changeMode(newMode, command.zoomMeetingId(), classroom);

        // 5. Add reason to notes if provided
        if (command.reason() != null && !command.reason().isBlank()) {
            String changeNote = String.format(
                    "[Mode Change] %s → %s: %s",
                    oldMode, newMode, command.reason()
            );
            String existingNotes = updatedSession.getNotes() != null ?
                    updatedSession.getNotes() + "\n" : "";
            updatedSession = updatedSession.updateNotes(existingNotes + changeNote);
        }

        // 6. Save
        SessionDomain saved = repository.save(updatedSession);

        log.info("Session {} mode changed from {} to {}", saved.getId(), oldMode, newMode);
        return saved;
    }
}
