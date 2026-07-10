package com.acainfo.reservation.infrastructure.adapter.in.rest;

import com.acainfo.reservation.application.port.in.GenerateReservationsUseCase;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.ReservationResponse;
import com.acainfo.reservation.infrastructure.mapper.ReservationRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Reservation Generation operations.
 * Endpoints: /api/sessions/{sessionId}/reservations/generate
 *
 * Handles: auto-generation of reservations when sessions are created.
 */
@RestController
@RequestMapping("/api/sessions/{sessionId}/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationGenerationController {

    private final GenerateReservationsUseCase generateReservationsUseCase;
    private final ReservationRestMapper reservationRestMapper;

    /**
     * Generate reservations for a session from enrolled students.
     * POST /api/sessions/{sessionId}/reservations/generate?courseId=1
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponse>> generate(
            @PathVariable Long sessionId,
            @RequestParam Long courseId
    ) {
        log.info("REST: Generating reservations for session {} from group {}", sessionId, courseId);

        List<SessionReservation> reservations = generateReservationsUseCase.generate(
                reservationRestMapper.toGenerateCommand(sessionId, courseId));
        List<ReservationResponse> responses = reservationRestMapper.toResponseList(reservations);

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}
