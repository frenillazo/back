package com.acainfo.reservation.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.reservation.application.dto.CreateReservationCommand;
import com.acainfo.reservation.application.dto.SwitchSessionCommand;
import com.acainfo.reservation.application.port.in.CancelReservationUseCase;
import com.acainfo.reservation.application.port.in.CreateReservationUseCase;
import com.acainfo.reservation.application.port.in.SwitchSessionUseCase;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.exception.CrossGroupReservationNotAllowedException;
import com.acainfo.reservation.domain.exception.InvalidReservationStateException;
import com.acainfo.reservation.domain.exception.ReservationAlreadyExistsException;
import com.acainfo.reservation.domain.exception.ReservationNotFoundException;
import com.acainfo.reservation.domain.exception.SessionFullException;
import com.acainfo.reservation.domain.exception.SubjectReservationAlreadyExistsException;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementing reservation management use cases.
 * Handles create, cancel, and switch session operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService implements
        CreateReservationUseCase,
        CancelReservationUseCase,
        SwitchSessionUseCase {

    private static final int MAX_IN_PERSON_CAPACITY = 24;

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;
    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;

    // ==================== CreateReservationUseCase ====================

    @Override
    @Transactional
    public SessionReservation create(CreateReservationCommand command) {
        log.info("Creating reservation: studentId={}, sessionId={}, mode={}",
                command.studentId(), command.sessionId(), command.mode());

        // Validate session exists
        Session session = sessionRepositoryPort.findById(command.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // Check if already reserved
        if (reservationRepositoryPort.existsByStudentIdAndSessionId(command.studentId(), command.sessionId())) {
            throw new ReservationAlreadyExistsException(command.studentId(), command.sessionId());
        }

        // Validate enrollment and subject match
        Enrollment enrollment = enrollmentRepositoryPort.findById(command.enrollmentId())
                .orElseThrow(() -> new InvalidReservationStateException(
                        "Enrollment not found: " + command.enrollmentId()));

        validateCrossGroupReservation(enrollment, session, command.studentId());

        // Check subject-level uniqueness: only one CONFIRMED reservation per subject
        if (reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(
                command.studentId(), session.getSubjectId())) {
            throw new SubjectReservationAlreadyExistsException(command.studentId(), session.getSubjectId());
        }

        // Check in-person capacity if needed
        if (command.mode() == ReservationMode.IN_PERSON) {
            validateInPersonCapacity(command.sessionId());
        }

        SessionReservation reservation = SessionReservation.builder()
                .studentId(command.studentId())
                .sessionId(command.sessionId())
                .enrollmentId(command.enrollmentId())
                .mode(command.mode())
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .build();

        SessionReservation saved = reservationRepositoryPort.save(reservation);

        log.info("Reservation created: id={}, studentId={}, sessionId={}, mode={}",
                saved.getId(), command.studentId(), command.sessionId(), command.mode());

        return saved;
    }

    // ==================== CancelReservationUseCase ====================

    @Override
    @Transactional
    public SessionReservation cancel(Long reservationId, Long studentId) {
        log.info("Cancelling reservation: id={}, studentId={}", reservationId, studentId);

        SessionReservation reservation = reservationRepositoryPort.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // Verify ownership
        if (!reservation.getStudentId().equals(studentId)) {
            throw new InvalidReservationStateException(
                    "Reservation " + reservationId + " does not belong to student " + studentId);
        }

        if (!reservation.canBeCancelled()) {
            throw new InvalidReservationStateException(
                    reservationId, reservation.getStatus().name(), "cancel");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        SessionReservation saved = reservationRepositoryPort.save(reservation);

        log.info("Reservation cancelled: id={}", reservationId);

        return saved;
    }

    // ==================== SwitchSessionUseCase ====================

    @Override
    @Transactional
    public SessionReservation switchSession(SwitchSessionCommand command) {
        log.info("Switching session: studentId={}, currentReservationId={}, newSessionId={}",
                command.studentId(), command.currentReservationId(), command.newSessionId());

        // Get current reservation
        SessionReservation currentReservation = reservationRepositoryPort.findById(command.currentReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(command.currentReservationId()));

        // Verify ownership
        if (!currentReservation.getStudentId().equals(command.studentId())) {
            throw new InvalidReservationStateException(
                    "Reservation " + command.currentReservationId() + " does not belong to student " + command.studentId());
        }

        // Validate new session exists
        Session newSession = sessionRepositoryPort.findById(command.newSessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.newSessionId()));

        // Check not already reserved
        if (reservationRepositoryPort.existsByStudentIdAndSessionId(command.studentId(), command.newSessionId())) {
            throw new ReservationAlreadyExistsException(command.studentId(), command.newSessionId());
        }

        // Validate same subject (cross-group allowed only within same subject)
        Enrollment enrollment = enrollmentRepositoryPort.findById(currentReservation.getEnrollmentId())
                .orElseThrow(() -> new InvalidReservationStateException(
                        "Enrollment not found: " + currentReservation.getEnrollmentId()));

        validateCrossGroupReservation(enrollment, newSession, command.studentId());

        // Check in-person capacity if current was in-person
        if (currentReservation.isInPerson()) {
            validateInPersonCapacity(command.newSessionId());
        }

        // Cancel current reservation
        currentReservation.setStatus(ReservationStatus.CANCELLED);
        currentReservation.setCancelledAt(LocalDateTime.now());
        reservationRepositoryPort.save(currentReservation);

        // Create new reservation
        SessionReservation newReservation = SessionReservation.builder()
                .studentId(command.studentId())
                .sessionId(command.newSessionId())
                .enrollmentId(currentReservation.getEnrollmentId())
                .mode(currentReservation.getMode())
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .build();

        SessionReservation saved = reservationRepositoryPort.save(newReservation);

        log.info("Session switched: studentId={}, oldSessionId={}, newSessionId={}",
                command.studentId(), currentReservation.getSessionId(), command.newSessionId());

        return saved;
    }

    // ==================== Private Helpers ====================

    private void validateCrossGroupReservation(Enrollment enrollment, Session session, Long studentId) {
        // Get the subject from the enrollment's group
        Long enrollmentSubjectId = getSubjectIdFromEnrollment(enrollment);

        // Session's subjectId should match enrollment's subject
        if (!session.getSubjectId().equals(enrollmentSubjectId)) {
            throw new CrossGroupReservationNotAllowedException(studentId, session.getId());
        }
    }

    private Long getSubjectIdFromEnrollment(Enrollment enrollment) {
        return groupRepositoryPort.findById(enrollment.getGroupId())
                .orElseThrow(() -> new InvalidReservationStateException(
                        "Group not found: " + enrollment.getGroupId()))
                .getSubjectId();
    }

    private void validateInPersonCapacity(Long sessionId) {
        long inPersonCount = reservationRepositoryPort.countInPersonReservations(sessionId);
        if (inPersonCount >= MAX_IN_PERSON_CAPACITY) {
            throw new SessionFullException(sessionId, MAX_IN_PERSON_CAPACITY);
        }
    }
}
