package acainfo.back.session.application.usecases;

import acainfo.back.session.application.ports.in.CompleteSessionUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.SessionDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CompleteSessionUseCase
 * Handles completing a session (EN_CURSO → COMPLETADA)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompleteSessionUseCaseImpl implements CompleteSessionUseCase {

    private final SessionRepositoryPort repository;

    @Override
    public SessionDomain completeSession(CompleteSessionCommand command) {
        log.info("Completing session {}", command.sessionId());

        // 1. Validate topics covered
        if (command.topicsCovered() == null || command.topicsCovered().isBlank()) {
            throw new IllegalArgumentException("Topics covered is required to complete a session");
        }

        // 2. Fetch session
        SessionDomain session = repository.findById(command.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 3. Complete session (domain method validates state)
        SessionDomain completedSession = session.complete(command.topicsCovered());

        // 4. Add additional notes if provided
        if (command.notes() != null && !command.notes().isBlank()) {
            String existingNotes = completedSession.getNotes() != null ?
                    completedSession.getNotes() + "\n" : "";
            completedSession = completedSession.updateNotes(existingNotes + command.notes());
        }

        // 5. Save
        SessionDomain saved = repository.save(completedSession);

        log.info("Session {} completed successfully at {}", saved.getId(), saved.getActualEnd());
        return saved;
    }
}
