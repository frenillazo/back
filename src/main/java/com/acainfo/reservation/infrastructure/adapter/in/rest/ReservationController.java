package com.acainfo.reservation.infrastructure.adapter.in.rest;

import com.acainfo.reservation.application.dto.ReservationFilters;
import com.acainfo.reservation.application.port.in.CancelReservationUseCase;
import com.acainfo.reservation.application.port.in.CreateReservationUseCase;
import com.acainfo.reservation.application.port.in.GetReservationUseCase;
import com.acainfo.reservation.application.port.in.SwitchSessionUseCase;
import com.acainfo.reservation.domain.model.AttendanceStatus;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.CreateReservationRequest;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.EnrichedReservationResponse;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.ReservationResponse;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.SwitchSessionRequest;
import com.acainfo.reservation.infrastructure.mapper.ReservationRestMapper;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Reservation operations.
 * Endpoints: /api/reservations
 *
 * Handles: create, cancel, switch session, and query operations.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final SwitchSessionUseCase switchSessionUseCase;
    private final GetReservationUseCase getReservationUseCase;
    private final ReservationRestMapper reservationRestMapper;
    private final ReservationResponseEnricher reservationResponseEnricher;
    private final ReservationSessionEnricher reservationSessionEnricher;

    /**
     * Create a new reservation.
     * POST /api/reservations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        log.info("REST: Creating reservation for student {} in session {}",
                request.getStudentId(), request.getSessionId());

        SessionReservation reservation = createReservationUseCase.create(
                reservationRestMapper.toCommand(request));
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponseEnricher.enrich(response));
    }

    /**
     * Get reservation by ID.
     * GET /api/reservations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        log.debug("REST: Getting reservation by ID: {}", id);

        SessionReservation reservation = getReservationUseCase.getById(id);
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.ok(reservationResponseEnricher.enrich(response));
    }

    /**
     * Get reservations with filters.
     * GET /api/reservations?studentId=1&sessionId=2&status=CONFIRMED&...
     */
    @GetMapping
    public ResponseEntity<PageResponse<ReservationResponse>> getWithFilters(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long enrollmentId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) ReservationMode mode,
            @RequestParam(required = false) OnlineRequestStatus onlineRequestStatus,
            @RequestParam(required = false) AttendanceStatus attendanceStatus,
            @RequestParam(required = false) Boolean hasAttendanceRecorded,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reservedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("REST: Getting reservations with filters - studentId={}, sessionId={}, status={}",
                studentId, sessionId, status);

        ReservationFilters filters = new ReservationFilters(
                studentId, sessionId, enrollmentId, status, mode,
                onlineRequestStatus, attendanceStatus, hasAttendanceRecorded,
                page, size, sortBy, sortDirection
        );

        PageResponse<SessionReservation> pageResult = getReservationUseCase.findWithFilters(filters);
        PageResponse<ReservationResponse> response = new PageResponse<>(
                reservationResponseEnricher.enrichList(reservationRestMapper.toResponseList(pageResult.content())),
                pageResult.pageNumber(),
                pageResult.pageSize(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.first(),
                pageResult.last()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get reservations for a session.
     * GET /api/reservations/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ReservationResponse>> getBySessionId(@PathVariable Long sessionId) {
        log.debug("REST: Getting reservations for session: {}", sessionId);

        List<SessionReservation> reservations = getReservationUseCase.getBySessionId(sessionId);
        List<ReservationResponse> responses = reservationRestMapper.toResponseList(reservations);

        return ResponseEntity.ok(reservationResponseEnricher.enrichList(responses));
    }

    /**
     * Get reservations for a student.
     * GET /api/reservations/student/{studentId}
     * Students can only see their own reservations; admins can see any.
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or #studentId == authentication.principal.userId")
    public ResponseEntity<List<ReservationResponse>> getByStudentId(@PathVariable Long studentId) {
        log.debug("REST: Getting reservations for student: {}", studentId);

        List<SessionReservation> reservations = getReservationUseCase.getByStudentId(studentId);
        List<ReservationResponse> responses = reservationRestMapper.toResponseList(reservations);

        return ResponseEntity.ok(reservationResponseEnricher.enrichList(responses));
    }

    /**
     * Get enriched reservations for a student (includes session, subject, group, teacher data).
     * GET /api/reservations/student/{studentId}/enriched
     * Used by the student attendance history page.
     */
    @GetMapping("/student/{studentId}/enriched")
    @PreAuthorize("hasRole('ADMIN') or #studentId == authentication.principal.userId")
    public ResponseEntity<List<EnrichedReservationResponse>> getEnrichedByStudentId(@PathVariable Long studentId) {
        log.debug("REST: Getting enriched reservations for student: {}", studentId);

        List<SessionReservation> reservations = getReservationUseCase.getByStudentId(studentId);
        List<ReservationResponse> responses = reservationResponseEnricher.enrichList(
                reservationRestMapper.toResponseList(reservations));
        List<EnrichedReservationResponse> enriched = reservationSessionEnricher.enrichWithSessionData(responses);

        return ResponseEntity.ok(enriched);
    }

    /**
     * Cancel a reservation.
     * DELETE /api/reservations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long id,
            @RequestParam Long studentId
    ) {
        log.info("REST: Cancelling reservation: {}", id);

        SessionReservation reservation = cancelReservationUseCase.cancel(id, studentId);
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.ok(reservationResponseEnricher.enrich(response));
    }

    /**
     * Switch to a different session.
     * PUT /api/reservations/{id}/switch-session
     */
    @PutMapping("/{id}/switch-session")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ReservationResponse> switchSession(
            @PathVariable Long id,
            @RequestParam Long studentId,
            @Valid @RequestBody SwitchSessionRequest request
    ) {
        log.info("REST: Switching reservation {} to session {}", id, request.getNewSessionId());

        SessionReservation reservation = switchSessionUseCase.switchSession(
                reservationRestMapper.toSwitchCommand(id, studentId, request));
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.ok(reservationResponseEnricher.enrich(response));
    }
}
