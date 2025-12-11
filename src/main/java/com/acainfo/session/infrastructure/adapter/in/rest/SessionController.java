package com.acainfo.session.infrastructure.adapter.in.rest;

import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.application.port.in.*;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.*;
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
 * REST Controller for Session management.
 * Endpoints: /api/sessions
 *
 * Security:
 * - GET (all, by id): Authenticated users
 * - POST, PUT, DELETE, lifecycle operations: ADMIN or TEACHER
 * - Generate sessions: ADMIN only
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
    private final SessionLifecycleUseCase sessionLifecycleUseCase;
    private final GenerateSessionsUseCase generateSessionsUseCase;
    private final SessionRestMapper sessionRestMapper;

    // ==================== CRUD Operations ====================

    /**
     * Create a new session.
     * POST /api/sessions
     *
     * @param request CreateSessionRequest
     * @return SessionResponse with 201 CREATED
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
     *
     * @param id Session ID
     * @return SessionResponse with 200 OK
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
     *
     * @param subjectId Filter by subject ID (optional)
     * @param groupId Filter by group ID (optional)
     * @param scheduleId Filter by schedule ID (optional)
     * @param type Filter by session type (optional)
     * @param status Filter by status (optional)
     * @param mode Filter by mode (optional)
     * @param dateFrom Filter sessions from date (optional)
     * @param dateTo Filter sessions to date (optional)
     * @param page Page number (default 0)
     * @param size Page size (default 20)
     * @param sortBy Sort field (default "date")
     * @param sortDirection Sort direction (default "ASC")
     * @return Page of SessionResponse with 200 OK
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
                subjectId,
                groupId,
                scheduleId,
                type,
                status,
                mode,
                dateFrom,
                dateTo,
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<Session> sessionsPage = getSessionUseCase.findWithFilters(filters);
        Page<SessionResponse> responsePage = sessionsPage.map(sessionRestMapper::toResponse);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get sessions by group ID.
     * GET /api/sessions/group/{groupId}
     *
     * @param groupId Group ID
     * @return List of SessionResponse with 200 OK
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
     *
     * @param subjectId Subject ID
     * @return List of SessionResponse with 200 OK
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
     *
     * @param scheduleId Schedule ID
     * @return List of SessionResponse with 200 OK
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
     *
     * @param id Session ID
     * @param request UpdateSessionRequest
     * @return SessionResponse with 200 OK
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
     *
     * @param id Session ID
     * @return 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("REST: Deleting session ID: {}", id);

        deleteSessionUseCase.delete(id);

        return ResponseEntity.noContent().build();
    }

    // ==================== Lifecycle Operations ====================

    /**
     * Start session (SCHEDULED -> IN_PROGRESS).
     * POST /api/sessions/{id}/start
     *
     * @param id Session ID
     * @return SessionResponse with 200 OK
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
     *
     * @param id Session ID
     * @return SessionResponse with 200 OK
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
     *
     * @param id Session ID
     * @return SessionResponse with 200 OK
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
     * @param id Session ID
     * @param request PostponeSessionRequest
     * @return SessionResponse (the new session) with 200 OK
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

    // ==================== Generation Operations ====================

    /**
     * Generate sessions from schedules for a date range.
     * POST /api/sessions/generate
     *
     * @param request GenerateSessionsRequest
     * @return List of created SessionResponses with 201 CREATED
     */
    @PostMapping("/generate")
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
     * @param request GenerateSessionsRequest
     * @return List of SessionResponses that would be created with 200 OK
     */
    @PostMapping("/generate/preview")
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
