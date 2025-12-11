package com.acainfo.session.infrastructure.adapter.in.rest;

import com.acainfo.session.application.port.in.GenerateSessionsUseCase;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.GenerateSessionsRequest;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.SessionResponse;
import com.acainfo.session.infrastructure.mapper.SessionRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Session generation operations.
 * Handles bulk creation of REGULAR sessions from schedules.
 * Endpoints: /api/sessions/generate
 *
 * Security: ADMIN only
 */
@RestController
@RequestMapping("/api/sessions/generate")
@RequiredArgsConstructor
@Slf4j
public class SessionGenerationController {

    private final GenerateSessionsUseCase generateSessionsUseCase;
    private final SessionRestMapper sessionRestMapper;

    /**
     * Generate sessions from schedules for a date range.
     * POST /api/sessions/generate
     *
     * @return List of created sessions
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> generateSessions(
            @Valid @RequestBody GenerateSessionsRequest request
    ) {
        log.info("REST: Generating sessions for group: {}, from: {}, to: {}",
                request.getGroupId(), request.getStartDate(), request.getEndDate());

        List<Session> generatedSessions = generateSessionsUseCase.generate(
                sessionRestMapper.toCommand(request)
        );
        List<SessionResponse> responses = sessionRestMapper.toResponseList(generatedSessions);

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Preview sessions that would be generated (dry run).
     * POST /api/sessions/generate/preview
     *
     * @return List of sessions that would be created (not persisted)
     */
    @PostMapping("/preview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> previewGenerateSessions(
            @Valid @RequestBody GenerateSessionsRequest request
    ) {
        log.info("REST: Previewing session generation for group: {}, from: {}, to: {}",
                request.getGroupId(), request.getStartDate(), request.getEndDate());

        List<Session> previewSessions = generateSessionsUseCase.preview(
                sessionRestMapper.toCommand(request)
        );
        List<SessionResponse> responses = sessionRestMapper.toResponseList(previewSessions);

        return ResponseEntity.ok(responses);
    }
}
