package com.acainfo.reservation.application.service;

import com.acainfo.reservation.application.dto.BulkRecordAttendanceCommand;
import com.acainfo.reservation.application.dto.RecordAttendanceCommand;
import com.acainfo.reservation.application.port.in.RecordAttendanceUseCase;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.exception.AttendanceAlreadyRecordedException;
import com.acainfo.reservation.domain.exception.ReservationNotFoundException;
import com.acainfo.reservation.domain.model.AttendanceStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service implementing attendance recording use cases.
 * Handles single and bulk attendance recording for sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService implements RecordAttendanceUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    // ==================== RecordAttendanceUseCase ====================

    @Override
    @Transactional
    public SessionReservation recordSingle(RecordAttendanceCommand command) {
        log.info("Recording attendance: reservationId={}, status={}, recordedBy={}",
                command.reservationId(), command.status(), command.recordedById());

        SessionReservation reservation = reservationRepositoryPort.findById(command.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(command.reservationId()));

        if (reservation.hasAttendanceRecorded()) {
            throw new AttendanceAlreadyRecordedException(command.reservationId());
        }

        reservation.setAttendanceStatus(command.status());
        reservation.setAttendanceRecordedAt(LocalDateTime.now());
        reservation.setAttendanceRecordedById(command.recordedById());

        SessionReservation saved = reservationRepositoryPort.save(reservation);

        log.info("Attendance recorded: reservationId={}, status={}",
                command.reservationId(), command.status());

        return saved;
    }

    @Override
    @Transactional
    public List<SessionReservation> recordBulk(BulkRecordAttendanceCommand command) {
        log.info("Recording bulk attendance: sessionId={}, count={}, recordedBy={}",
                command.sessionId(), command.attendanceMap().size(), command.recordedById());

        List<SessionReservation> updatedReservations = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Long, AttendanceStatus> entry : command.attendanceMap().entrySet()) {
            Long reservationId = entry.getKey();
            AttendanceStatus status = entry.getValue();

            SessionReservation reservation = reservationRepositoryPort.findById(reservationId)
                    .orElseThrow(() -> new ReservationNotFoundException(reservationId));

            // Validate reservation belongs to the session
            if (!reservation.getSessionId().equals(command.sessionId())) {
                log.warn("Reservation {} does not belong to session {}, skipping",
                        reservationId, command.sessionId());
                continue;
            }

            if (reservation.hasAttendanceRecorded()) {
                log.warn("Attendance already recorded for reservation {}, skipping", reservationId);
                continue;
            }

            reservation.setAttendanceStatus(status);
            reservation.setAttendanceRecordedAt(now);
            reservation.setAttendanceRecordedById(command.recordedById());

            updatedReservations.add(reservation);
        }

        List<SessionReservation> saved = reservationRepositoryPort.saveAll(updatedReservations);

        log.info("Bulk attendance recorded: sessionId={}, recorded={} of {}",
                command.sessionId(), saved.size(), command.attendanceMap().size());

        return saved;
    }
}
