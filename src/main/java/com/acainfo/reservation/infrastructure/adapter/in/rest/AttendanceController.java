package com.acainfo.reservation.infrastructure.adapter.in.rest;

import com.acainfo.reservation.application.port.in.GetReservationUseCase;
import com.acainfo.reservation.application.port.in.RecordAttendanceUseCase;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.BulkRecordAttendanceRequest;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.RecordAttendanceRequest;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.ReservationResponse;
import com.acainfo.reservation.infrastructure.mapper.ReservationRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Attendance operations.
 * Endpoints: /api/reservations/{id}/attendance, /api/sessions/{sessionId}/attendance
 *
 * Handles: record single attendance, bulk attendance recording.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final RecordAttendanceUseCase recordAttendanceUseCase;
    private final GetReservationUseCase getReservationUseCase;
    private final ReservationRestMapper reservationRestMapper;
    private final ReservationResponseEnricher reservationResponseEnricher;

    /**
     * Record attendance for a single reservation.
     * PUT /api/reservations/{id}/attendance
     */
    @PutMapping("/api/reservations/{id}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReservationResponse> recordSingle(
            @PathVariable Long id,
            @RequestParam Long recordedById,
            @Valid @RequestBody RecordAttendanceRequest request
    ) {
        log.info("REST: Recording attendance for reservation {}: status={}",
                id, request.getStatus());

        SessionReservation reservation = recordAttendanceUseCase.recordSingle(
                reservationRestMapper.toRecordCommand(id, recordedById, request));
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.ok(reservationResponseEnricher.enrich(response));
    }

    /**
     * Record attendance for all reservations in a session.
     * POST /api/sessions/{sessionId}/attendance
     */
    @PostMapping("/api/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReservationResponse>> recordBulk(
            @PathVariable Long sessionId,
            @RequestParam Long recordedById,
            @Valid @RequestBody BulkRecordAttendanceRequest request
    ) {
        log.info("REST: Recording bulk attendance for session {}: count={}",
                sessionId, request.getAttendanceMap().size());

        List<SessionReservation> reservations = recordAttendanceUseCase.recordBulk(
                reservationRestMapper.toBulkRecordCommand(sessionId, recordedById, request));
        List<ReservationResponse> responses = reservationRestMapper.toResponseList(reservations);

        return ResponseEntity.ok(reservationResponseEnricher.enrichList(responses));
    }

    /**
     * Get reservations for a session (for attendance taking).
     * GET /api/sessions/{sessionId}/attendance
     */
    @GetMapping("/api/sessions/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReservationResponse>> getSessionAttendance(@PathVariable Long sessionId) {
        log.debug("REST: Getting attendance for session: {}", sessionId);

        List<SessionReservation> reservations = getReservationUseCase.getBySessionId(sessionId);
        List<ReservationResponse> responses = reservationRestMapper.toResponseList(reservations);

        return ResponseEntity.ok(reservationResponseEnricher.enrichList(responses));
    }
}
