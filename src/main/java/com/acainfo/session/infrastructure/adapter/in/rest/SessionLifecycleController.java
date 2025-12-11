package com.acainfo.session.infrastructure.adapter.in.rest;

import com.acainfo.session.application.port.in.SessionLifecycleUseCase;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.PostponeSessionRequest;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.SessionResponse;
import com.acainfo.session.infrastructure.mapper.SessionRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Session lifecycle operations.
 * Handles state transitions: start, complete, cancel, postpone.
 * Endpoints: /api/sessions/{id}/...
 *
 * Security: ADMIN or TEACHER
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionLifecycleController {

    private final SessionLifecycleUseCase sessionLifecycleUseCase;
    private final SessionRestMapper sessionRestMapper;

    /**
     * Start session (SCHEDULED -> IN_PROGRESS).
     * POST /api/sessions/{id}/start
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> startSession(@PathVariable Long id) {
        log.info("REST: Starting session ID: {}", id);

        Session startedSession = sessionLifecycleUseCase.start(id);
        SessionResponse response = sessionRestMapper.toResponse(startedSession);

        return ResponseEntity.ok(response);
    }

    /**
     * Complete session (IN_PROGRESS -> COMPLETED).
     * POST /api/sessions/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> completeSession(@PathVariable Long id) {
        log.info("REST: Completing session ID: {}", id);

        Session completedSession = sessionLifecycleUseCase.complete(id);
        SessionResponse response = sessionRestMapper.toResponse(completedSession);

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel session (SCHEDULED -> CANCELLED).
     * POST /api/sessions/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> cancelSession(@PathVariable Long id) {
        log.info("REST: Cancelling session ID: {}", id);

        Session cancelledSession = sessionLifecycleUseCase.cancel(id);
        SessionResponse response = sessionRestMapper.toResponse(cancelledSession);

        return ResponseEntity.ok(response);
    }

    /**
     * Postpone session to a new date.
     * POST /api/sessions/{id}/postpone
     *
     * @return The newly created session with the new date
     */
    @PostMapping("/{id}/postpone")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> postponeSession(
            @PathVariable Long id,
            @Valid @RequestBody PostponeSessionRequest request
    ) {
        log.info("REST: Postponing session ID: {} to date: {}", id, request.getNewDate());

        Session newSession = sessionLifecycleUseCase.postpone(id, sessionRestMapper.toCommand(request));
        SessionResponse response = sessionRestMapper.toResponse(newSession);

        return ResponseEntity.ok(response);
    }
}
