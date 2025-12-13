package com.acainfo.reservation.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.reservation.application.dto.GenerateReservationsCommand;
import com.acainfo.reservation.application.port.in.GenerateReservationsUseCase;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementing automatic reservation generation.
 * Called when sessions are created from schedules to auto-generate
 * reservations for all enrolled students.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationGeneratorService implements GenerateReservationsUseCase {

    private static final int MAX_IN_PERSON_CAPACITY = 24;

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final EnrollmentRepositoryPort enrollmentRepositoryPort;

    // ==================== GenerateReservationsUseCase ====================

    @Override
    @Transactional
    public List<SessionReservation> generate(GenerateReservationsCommand command) {
        log.info("Generating reservations for session {} from group {}",
                command.sessionId(), command.groupId());

        // Get all active enrollments for the group
        List<Enrollment> activeEnrollments = enrollmentRepositoryPort
                .findByGroupIdAndStatus(command.groupId(), EnrollmentStatus.ACTIVE);

        if (activeEnrollments.isEmpty()) {
            log.info("No active enrollments for group {}, no reservations generated",
                    command.groupId());
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<SessionReservation> reservations = new ArrayList<>();
        int inPersonCount = 0;

        for (Enrollment enrollment : activeEnrollments) {
            // Skip if reservation already exists (idempotency)
            if (reservationRepositoryPort.existsByStudentIdAndSessionId(
                    enrollment.getStudentId(), command.sessionId())) {
                log.debug("Reservation already exists for student {} in session {}, skipping",
                        enrollment.getStudentId(), command.sessionId());
                continue;
            }

            // Determine mode: first 24 get IN_PERSON, rest get ONLINE
            ReservationMode mode;
            if (inPersonCount < MAX_IN_PERSON_CAPACITY) {
                mode = ReservationMode.IN_PERSON;
                inPersonCount++;
            } else {
                mode = ReservationMode.ONLINE;
            }

            SessionReservation reservation = SessionReservation.builder()
                    .studentId(enrollment.getStudentId())
                    .sessionId(command.sessionId())
                    .enrollmentId(enrollment.getId())
                    .mode(mode)
                    .status(ReservationStatus.CONFIRMED)
                    .reservedAt(now)
                    .build();

            reservations.add(reservation);
        }

        List<SessionReservation> saved = reservationRepositoryPort.saveAll(reservations);

        log.info("Generated {} reservations for session {} ({} in-person, {} online)",
                saved.size(), command.sessionId(),
                Math.min(inPersonCount, saved.size()),
                Math.max(0, saved.size() - MAX_IN_PERSON_CAPACITY));

        return saved;
    }
}
