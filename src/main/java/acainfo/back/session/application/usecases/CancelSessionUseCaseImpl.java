package acainfo.back.session.application.usecases;

import acainfo.back.session.application.ports.in.CancelSessionUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.SessionDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CancelSessionUseCase
 * Handles cancelling sessions (PROGRAMADA → CANCELADA)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CancelSessionUseCaseImpl implements CancelSessionUseCase {

    private final SessionRepositoryPort repository;

    @Override
    public SessionDomain cancelSession(CancelSessionCommand command) {
        log.info("Cancelling session {}", command.sessionId());

        // 1. Validate reason
        if (command.reason() == null || command.reason().isBlank()) {
            throw new IllegalArgumentException("Reason is required to cancel a session");
        }

        // 2. Fetch session
        SessionDomain session = repository.findById(command.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 3. Cancel session (domain method validates state)
        SessionDomain cancelledSession = session.cancel(command.reason());

        // 4. Save
        SessionDomain saved = repository.save(cancelledSession);

        log.info("Session {} cancelled. Reason: {}", saved.getId(), command.reason());
        return saved;
    }
}
