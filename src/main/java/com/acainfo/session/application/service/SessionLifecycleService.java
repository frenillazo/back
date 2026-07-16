package com.acainfo.session.application.service;

import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.dto.PostponeSessionCommand;
import com.acainfo.session.application.port.in.GetSessionUseCase;
import com.acainfo.session.application.port.in.SessionLifecycleUseCase;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing session lifecycle transitions.
 * Handles state changes: start, complete, cancel, postpone.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionLifecycleService implements SessionLifecycleUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final GetSessionUseCase getSessionUseCase;
    private final ReservationRepositoryPort reservationRepositoryPort;

    @Override
    @Transactional
    public Session start(Long id) {
        log.info("Starting session with ID: {}", id);

        Session session = getSessionUseCase.getById(id);

        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Solo se pueden iniciar sesiones SCHEDULED. Estado actual: " + session.getStatus()
            );
        }

        session.setStatus(SessionStatus.IN_PROGRESS);
        Session updatedSession = sessionRepositoryPort.save(session);

        log.info("Session started successfully: ID {}", id);
        return updatedSession;
    }

    @Override
    @Transactional
    public Session complete(Long id) {
        log.info("Completing session with ID: {}", id);

        Session session = getSessionUseCase.getById(id);

        if (!session.isInProgress()) {
            throw new InvalidSessionStateException(
                    "Solo se pueden completar sesiones IN_PROGRESS. Estado actual: " + session.getStatus()
            );
        }

        session.setStatus(SessionStatus.COMPLETED);
        Session updatedSession = sessionRepositoryPort.save(session);

        log.info("Session completed successfully: ID {}", id);
        return updatedSession;
    }

    @Override
    @Transactional
    public Session cancel(Long id) {
        log.info("Cancelling session with ID: {}", id);

        Session session = getSessionUseCase.getById(id);

        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Solo se pueden cancelar sesiones SCHEDULED. Estado actual: " + session.getStatus()
            );
        }

        session.setStatus(SessionStatus.CANCELLED);
        Session updatedSession = sessionRepositoryPort.save(session);

        log.info("Session cancelled successfully: ID {}", id);
        return updatedSession;
    }

    @Override
    @Transactional
    public Session postpone(Long id, PostponeSessionCommand command) {
        log.info("Postponing session with ID: {} to date: {}", id, command.newDate());

        Session originalSession = getSessionUseCase.getById(id);

        if (!originalSession.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Solo se pueden posponer sesiones SCHEDULED. Estado actual: " + originalSession.getStatus()
            );
        }

        // Mark original session as postponed
        originalSession.setStatus(SessionStatus.POSTPONED);
        originalSession.setPostponedToDate(command.newDate());
        sessionRepositoryPort.save(originalSession);

        // Create new session with the new date/time
        Session newSession = Session.builder()
                .subjectId(originalSession.getSubjectId())
                .courseId(originalSession.getCourseId())
                .scheduleId(originalSession.getScheduleId())
                .classroom(command.newClassroom() != null ? command.newClassroom() : originalSession.getClassroom())
                .date(command.newDate())
                .startTime(command.newStartTime() != null ? command.newStartTime() : originalSession.getStartTime())
                .endTime(command.newEndTime() != null ? command.newEndTime() : originalSession.getEndTime())
                .status(SessionStatus.SCHEDULED)
                .type(originalSession.getType())
                .mode(command.newMode() != null ? command.newMode() : originalSession.getMode())
                .build();

        Session savedNewSession = sessionRepositoryPort.save(newSession);

        migrateReservations(originalSession, savedNewSession);

        log.info("Session postponed successfully: original ID {}, new ID {}, new date {}",
                id, savedNewSession.getId(), command.newDate());

        return savedNewSession;
    }

    /**
     * Lleva las reservas confirmadas de la sesión pospuesta a la nueva.
     *
     * La sesión de reemplazo nacía sin reservas: el alumno veía "Pospuesta al X",
     * iba a la nueva y no tenía plaza, sin que nada se lo advirtiera. Las
     * reservas solo se generaban al aprobar la inscripción o al generar sesiones,
     * nunca aquí.
     *
     * Las reservas de la original se quedan como estaban: la sesión ya figura
     * como POSTPONED y sirven de histórico.
     */
    private void migrateReservations(Session originalSession, Session newSession) {
        List<SessionReservation> migrated = reservationRepositoryPort
                .findBySessionId(originalSession.getId())
                .stream()
                .filter(SessionReservation::isConfirmed)
                .map(reservation -> reservation.toBuilder()
                        .id(null)
                        .sessionId(newSession.getId())
                        .reservedAt(LocalDateTime.now())
                        .createdAt(null)
                        .updatedAt(null)
                        .build())
                .toList();

        if (migrated.isEmpty()) {
            return;
        }

        reservationRepositoryPort.saveAll(migrated);
        log.info("Migrated {} reservations from postponed session {} to new session {}",
                migrated.size(), originalSession.getId(), newSession.getId());
    }
}
