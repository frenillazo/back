package acainfo.back.session.application.usecases;

import acainfo.back.session.application.ports.in.StartSessionUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.SessionDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of StartSessionUseCase
 * Handles starting a session (PROGRAMADA → EN_CURSO)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StartSessionUseCaseImpl implements StartSessionUseCase {

    private final SessionRepositoryPort repository;

    @Override
    public SessionDomain startSession(StartSessionCommand command) {
        log.info("Starting session {}", command.sessionId());

        // 1. Fetch session
        SessionDomain session = repository.findById(command.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 2. Start session (domain method validates state)
        SessionDomain startedSession = session.start();

        // 3. Add notes if provided
        if (command.notes() != null && !command.notes().isBlank()) {
            startedSession = startedSession.updateNotes(command.notes());
        }

        // 4. Save
        SessionDomain saved = repository.save(startedSession);

        log.info("Session {} started successfully at {}", saved.getId(), saved.getActualStart());
        return saved;
    }
}
