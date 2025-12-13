package com.acainfo.session.infrastructure.adapter.in.rest;

import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.application.port.in.CreateSessionUseCase;
import com.acainfo.session.application.port.in.DeleteSessionUseCase;
import com.acainfo.session.application.port.in.GetSessionUseCase;
import com.acainfo.session.application.port.in.UpdateSessionUseCase;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.CreateSessionRequest;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.SessionResponse;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.UpdateSessionRequest;
import com.acainfo.session.infrastructure.mapper.SessionRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Session CRUD operations.
 * Endpoints: /api/sessions
 *
 * Security:
 * - GET: Authenticated users
 * - POST, PUT, DELETE: ADMIN or TEACHER
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final CreateSessionUseCase createSessionUseCase;
    private final GetSessionUseCase getSessionUseCase;
    private final UpdateSessionUseCase updateSessionUseCase;
    private final DeleteSessionUseCase deleteSessionUseCase;
    private final SessionRestMapper sessionRestMapper;

    /**
     * Create a new session.
     * POST /api/sessions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        log.info("REST: Creating session - type={}, groupId={}, date={}",
                request.getType(), request.getGroupId(), request.getDate());

        Session createdSession = createSessionUseCase.create(sessionRestMapper.toCommand(request));
        SessionResponse response = sessionRestMapper.toResponse(createdSession);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get session by ID.
     * GET /api/sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {
        log.debug("REST: Getting session by ID: {}", id);

        Session session = getSessionUseCase.getById(id);
        SessionResponse response = sessionRestMapper.toResponse(session);

        return ResponseEntity.ok(response);
    }

    /**
     * Get sessions with filters (pagination + sorting + filtering).
     * GET /api/sessions?subjectId=1&groupId=2&type=REGULAR&status=SCHEDULED&...
     */
    @GetMapping
    public ResponseEntity<Page<SessionResponse>> getSessionsWithFilters(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) SessionType type,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) SessionMode mode,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.debug("REST: Getting sessions with filters - groupId={}, status={}, dateFrom={}, dateTo={}",
                groupId, status, dateFrom, dateTo);

        SessionFilters filters = new SessionFilters(
                subjectId, groupId, null, scheduleId, type, status, mode,
                dateFrom, dateTo, page, size, sortBy, sortDirection
        );

        Page<Session> sessionsPage = getSessionUseCase.findWithFilters(filters);
        Page<SessionResponse> responsePage = sessionsPage.map(sessionRestMapper::toResponse);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get sessions by group ID.
     * GET /api/sessions/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByGroup(@PathVariable Long groupId) {
        log.debug("REST: Getting sessions for group: {}", groupId);

        List<Session> sessions = getSessionUseCase.findByGroupId(groupId);
        List<SessionResponse> responses = sessionRestMapper.toResponseList(sessions);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get sessions by subject ID.
     * GET /api/sessions/subject/{subjectId}
     */
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<SessionResponse>> getSessionsBySubject(@PathVariable Long subjectId) {
        log.debug("REST: Getting sessions for subject: {}", subjectId);

        List<Session> sessions = getSessionUseCase.findBySubjectId(subjectId);
        List<SessionResponse> responses = sessionRestMapper.toResponseList(sessions);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get sessions by schedule ID.
     * GET /api/sessions/schedule/{scheduleId}
     */
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<SessionResponse>> getSessionsBySchedule(@PathVariable Long scheduleId) {
        log.debug("REST: Getting sessions for schedule: {}", scheduleId);

        List<Session> sessions = getSessionUseCase.findByScheduleId(scheduleId);
        List<SessionResponse> responses = sessionRestMapper.toResponseList(sessions);

        return ResponseEntity.ok(responses);
    }

    /**
     * Update session.
     * PUT /api/sessions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionRequest request
    ) {
        log.info("REST: Updating session ID: {}", id);

        Session updatedSession = updateSessionUseCase.update(id, sessionRestMapper.toCommand(request));
        SessionResponse response = sessionRestMapper.toResponse(updatedSession);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete session.
     * DELETE /api/sessions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("REST: Deleting session ID: {}", id);

        deleteSessionUseCase.delete(id);

        return ResponseEntity.noContent().build();
    }
}
