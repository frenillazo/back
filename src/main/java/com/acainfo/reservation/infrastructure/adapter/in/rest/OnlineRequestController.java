package com.acainfo.reservation.infrastructure.adapter.in.rest;

import com.acainfo.reservation.application.port.in.GetReservationUseCase;
import com.acainfo.reservation.application.port.in.ProcessOnlineRequestUseCase;
import com.acainfo.reservation.application.port.in.RequestOnlineAttendanceUseCase;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.ProcessOnlineRequestRequest;
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
 * REST Controller for Online Attendance Request operations.
 * Endpoints: /api/reservations/{id}/online-request, /api/online-requests
 *
 * Handles: request online attendance, approve/reject requests.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class OnlineRequestController {

    private final RequestOnlineAttendanceUseCase requestOnlineAttendanceUseCase;
    private final ProcessOnlineRequestUseCase processOnlineRequestUseCase;
    private final GetReservationUseCase getReservationUseCase;
    private final ReservationRestMapper reservationRestMapper;
    private final ReservationResponseEnricher reservationResponseEnricher;

    /**
     * Request online attendance for a reservation.
     * POST /api/reservations/{id}/online-request
     */
    @PostMapping("/api/reservations/{id}/online-request")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ReservationResponse> requestOnline(
            @PathVariable Long id,
            @RequestParam Long studentId
    ) {
        log.info("REST: Requesting online attendance for reservation: {}", id);

        SessionReservation reservation = requestOnlineAttendanceUseCase.requestOnline(
                reservationRestMapper.toRequestOnlineCommand(id, studentId));
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.ok(reservationResponseEnricher.enrich(response));
    }

    /**
     * Process (approve/reject) an online attendance request.
     * PUT /api/reservations/{id}/online-request/process
     */
    @PutMapping("/api/reservations/{id}/online-request/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReservationResponse> processRequest(
            @PathVariable Long id,
            @RequestParam Long teacherId,
            @Valid @RequestBody ProcessOnlineRequestRequest request
    ) {
        log.info("REST: Processing online request for reservation {}: approved={}",
                id, request.getApproved());

        SessionReservation reservation = processOnlineRequestUseCase.process(
                reservationRestMapper.toProcessCommand(id, teacherId, request));
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.ok(reservationResponseEnricher.enrich(response));
    }

    /**
     * Get pending online requests for a teacher.
     * GET /api/online-requests/pending?teacherId=1
     */
    @GetMapping("/api/online-requests/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReservationResponse>> getPendingForTeacher(@RequestParam Long teacherId) {
        log.debug("REST: Getting pending online requests for teacher: {}", teacherId);

        List<SessionReservation> reservations = getReservationUseCase.getPendingOnlineRequestsForTeacher(teacherId);
        List<ReservationResponse> responses = reservationRestMapper.toResponseList(reservations);

        return ResponseEntity.ok(reservationResponseEnricher.enrichList(responses));
    }
}
