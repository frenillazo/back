package com.acainfo.reservation.application.service;

import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing automatic reservation generation and cancellation.
 * Implements AutoReservationPort from the enrollment module to maintain hexagonal architecture.
 *
 * <p>Called reactively when:</p>
 * <ul>
 *   <li>Enrollment is approved as ACTIVE → generates reservations for all future sessions</li>
 *   <li>Student is promoted from waiting list → generates reservations for all future sessions</li>
 *   <li>Enrollment is withdrawn → cancels all future confirmed reservations</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoReservationService implements AutoReservationPort {

    private static final int MAX_IN_PERSON_CAPACITY = 24;

    private final SessionRepositoryPort sessionRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;

    @Override
    @Transactional
    public void generateForNewEnrollment(Long studentId, Long groupId, Long enrollmentId) {
        log.info("Auto-generating reservations for student {} in group {} (enrollment {})",
                studentId, groupId, enrollmentId);

        List<Session> futureSessions = sessionRepositoryPort
                .findUpcomingByGroupIds(List.of(groupId), LocalDate.now(), 999);

        int created = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Session session : futureSessions) {
            // Idempotency: skip if reservation already exists
            if (reservationRepositoryPort.existsByStudentIdAndSessionId(studentId, session.getId())) {
                log.debug("Reservation already exists for student {} in session {}, skipping",
                        studentId, session.getId());
                continue;
            }

            // Determine mode: first 24 in-person seats, rest online
            long inPersonCount = reservationRepositoryPort.countInPersonReservations(session.getId());
            ReservationMode mode = inPersonCount < MAX_IN_PERSON_CAPACITY
                    ? ReservationMode.IN_PERSON
                    : ReservationMode.ONLINE;

            SessionReservation reservation = SessionReservation.builder()
                    .studentId(studentId)
                    .sessionId(session.getId())
                    .enrollmentId(enrollmentId)
                    .mode(mode)
                    .status(ReservationStatus.CONFIRMED)
                    .reservedAt(now)
                    .build();

            reservationRepositoryPort.save(reservation);
            created++;
        }

        log.info("Auto-generated {} reservations for student {} in group {}",
                created, studentId, groupId);
    }

    @Override
    @Transactional
    public void cancelFutureReservations(Long studentId, Long groupId) {
        log.info("Cancelling future reservations for student {} in group {}", studentId, groupId);

        List<Session> futureSessions = sessionRepositoryPort
                .findUpcomingByGroupIds(List.of(groupId), LocalDate.now(), 999);

        int cancelled = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Session session : futureSessions) {
            reservationRepositoryPort.findByStudentIdAndSessionId(studentId, session.getId())
                    .filter(SessionReservation::isConfirmed)
                    .ifPresent(reservation -> {
                        reservation.setStatus(ReservationStatus.CANCELLED);
                        reservation.setCancelledAt(now);
                        reservationRepositoryPort.save(reservation);
                    });
            cancelled++;
        }

        log.info("Cancelled {} future reservations for student {} in group {}",
                cancelled, studentId, groupId);
    }
}
