package com.acainfo.reservation.application.service;

import com.acainfo.reservation.application.dto.ProcessOnlineRequestCommand;
import com.acainfo.reservation.application.dto.RequestOnlineAttendanceCommand;
import com.acainfo.reservation.application.port.in.ProcessOnlineRequestUseCase;
import com.acainfo.reservation.application.port.in.RequestOnlineAttendanceUseCase;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.exception.InvalidReservationStateException;
import com.acainfo.reservation.domain.exception.OnlineRequestAlreadyExistsException;
import com.acainfo.reservation.domain.exception.OnlineRequestTooLateException;
import com.acainfo.reservation.domain.exception.ReservationNotFoundException;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service implementing online attendance request use cases.
 * Handles student requests to attend online and teacher approval/rejection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineRequestService implements
        RequestOnlineAttendanceUseCase,
        ProcessOnlineRequestUseCase {

    private static final int MINIMUM_HOURS_ADVANCE = 6;

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;

    // ==================== RequestOnlineAttendanceUseCase ====================

    @Override
    @Transactional
    public SessionReservation requestOnline(RequestOnlineAttendanceCommand command) {
        log.info("Requesting online attendance: reservationId={}, studentId={}",
                command.reservationId(), command.studentId());

        SessionReservation reservation = reservationRepositoryPort.findById(command.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(command.reservationId()));

        // Verify ownership
        if (!reservation.getStudentId().equals(command.studentId())) {
            throw new InvalidReservationStateException(
                    "Reservation " + command.reservationId() + " does not belong to student " + command.studentId());
        }

        // Must be confirmed and in-person
        if (!reservation.isConfirmed()) {
            throw new InvalidReservationStateException(
                    command.reservationId(), reservation.getStatus().name(), "request online attendance");
        }

        if (!reservation.isInPerson()) {
            throw new InvalidReservationStateException(
                    "Reservation " + command.reservationId() + " is already ONLINE");
        }

        // Check no existing request
        if (reservation.hasOnlineRequest()) {
            throw new OnlineRequestAlreadyExistsException(command.reservationId());
        }

        // Validate time constraint (6+ hours before session)
        validateTimeConstraint(reservation.getSessionId());

        // Create the request
        reservation.setOnlineRequestStatus(OnlineRequestStatus.PENDING);
        reservation.setOnlineRequestedAt(LocalDateTime.now());

        SessionReservation saved = reservationRepositoryPort.save(reservation);

        log.info("Online attendance request created: reservationId={}, status=PENDING",
                command.reservationId());

        return saved;
    }

    // ==================== ProcessOnlineRequestUseCase ====================

    @Override
    @Transactional
    public SessionReservation process(ProcessOnlineRequestCommand command) {
        log.info("Processing online request: reservationId={}, teacherId={}, approved={}",
                command.reservationId(), command.teacherId(), command.approved());

        SessionReservation reservation = reservationRepositoryPort.findById(command.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(command.reservationId()));

        // Must have a pending request
        if (!reservation.isOnlineRequestPending()) {
            throw new InvalidReservationStateException(
                    "Reservation " + command.reservationId() + " does not have a pending online request");
        }

        if (command.approved()) {
            reservation.setOnlineRequestStatus(OnlineRequestStatus.APPROVED);
            reservation.setMode(ReservationMode.ONLINE);
            log.info("Online request APPROVED: reservationId={}", command.reservationId());
        } else {
            reservation.setOnlineRequestStatus(OnlineRequestStatus.REJECTED);
            log.info("Online request REJECTED: reservationId={}", command.reservationId());
        }

        reservation.setOnlineRequestProcessedAt(LocalDateTime.now());
        reservation.setOnlineRequestProcessedById(command.teacherId());

        return reservationRepositoryPort.save(reservation);
    }

    // ==================== Private Helpers ====================

    private void validateTimeConstraint(Long sessionId) {
        Session session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        LocalDateTime sessionStart = LocalDateTime.of(session.getDate(), session.getStartTime());
        LocalDateTime deadline = sessionStart.minusHours(MINIMUM_HOURS_ADVANCE);

        if (LocalDateTime.now().isAfter(deadline)) {
            throw new OnlineRequestTooLateException(sessionId);
        }
    }
}
