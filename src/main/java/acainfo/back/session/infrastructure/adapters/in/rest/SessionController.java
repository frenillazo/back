package acainfo.back.session.infrastructure.adapters.in.rest;

import acainfo.back.session.application.ports.in.*;
import acainfo.back.session.application.services.SessionService;
import acainfo.back.session.domain.model.Session;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.infrastructure.adapters.in.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for session management.
 * Provides endpoints for CRUD operations and session lifecycle management.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sessions", description = "Session management API - CRUD and lifecycle operations")
@SecurityRequirement(name = "bearer-jwt")
public class SessionController {

    private final SessionService sessionService;

    // ==================== CREATE SESSION ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Create a new session",
        description = "Creates a new session for a subject group. Only ADMIN and TEACHER can create sessions."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Session created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Subject group not found"),
        @ApiResponse(responseCode = "409", description = "Schedule conflict detected")
    })
    public ResponseEntity<SessionResponse> createSession(
        @Valid @RequestBody CreateSessionRequest request,
        Authentication authentication
    ) {
        log.info("Creating new session for group {} by user {}",
            request.subjectGroupId(), authentication.getName());

        CreateSessionUseCase.CreateSessionCommand command = new CreateSessionUseCase.CreateSessionCommand(
            request.subjectGroupId(),
            request.type(),
            request.scheduledStart().toString(),
            request.scheduledEnd().toString(),
            request.mode(),
            request.classroom(),
            request.zoomMeetingId(),
            request.notes(),
            request.generatedFromScheduleId(),
            request.recoveryForSessionId(),
            request.originalSessionId()
        );

        Session session = sessionService.createSession(command);
        SessionResponse response = SessionResponse.fromEntity(session);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== GET SESSION BY ID ====================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get session by ID",
        description = "Retrieves detailed information about a specific session"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session found"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionResponse> getSessionById(
        @Parameter(description = "Session ID") @PathVariable Long id
    ) {
        log.debug("Fetching session with ID: {}", id);

        Session session = sessionService.findSessionById(id);
        SessionResponse response = SessionResponse.fromEntity(session);

        return ResponseEntity.ok(response);
    }

    // ==================== GET SESSIONS BY GROUP ====================

    @GetMapping("/group/{groupId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all sessions for a subject group",
        description = "Retrieves all sessions associated with a specific subject group"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<List<SessionResponse>> getSessionsByGroup(
        @Parameter(description = "Subject Group ID") @PathVariable Long groupId
    ) {
        log.debug("Fetching sessions for group: {}", groupId);

        List<Session> sessions = sessionService.getSessionsByGroup(groupId);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET SESSIONS BY GROUP AND DATE RANGE ====================

    @GetMapping("/group/{groupId}/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get sessions for a group within a date range",
        description = "Retrieves sessions for a specific group filtered by date range"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<List<SessionResponse>> getSessionsByGroupAndDateRange(
        @Parameter(description = "Subject Group ID") @PathVariable Long groupId,
        @Parameter(description = "Start date (ISO format)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @Parameter(description = "End date (ISO format)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.debug("Fetching sessions for group {} from {} to {}", groupId, startDate, endDate);

        List<Session> sessions = sessionService.getSessionsByGroupAndDateRange(groupId, startDate, endDate);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET SESSIONS BY TEACHER ====================

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get all sessions for a teacher",
        description = "Retrieves all sessions assigned to a specific teacher"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<SessionResponse>> getSessionsByTeacher(
        @Parameter(description = "Teacher ID") @PathVariable Long teacherId
    ) {
        log.debug("Fetching sessions for teacher: {}", teacherId);

        List<Session> sessions = sessionService.getSessionsByTeacher(teacherId);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET TEACHER'S OWN SESSIONS ====================

    @GetMapping("/teacher/me")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Get my sessions (teacher)",
        description = "Retrieves all sessions for the authenticated teacher"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "User is not a teacher")
    })
    public ResponseEntity<List<SessionResponse>> getMyTeacherSessions(Authentication authentication) {
        // TODO: Extract teacher ID from authentication
        log.debug("Fetching sessions for authenticated teacher: {}", authentication.getName());

        // For now, return empty list - will be implemented when User context is available
        return ResponseEntity.ok(List.of());
    }

    // ==================== GET SESSIONS BY TEACHER AND DATE RANGE ====================

    @GetMapping("/teacher/{teacherId}/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get sessions for a teacher within a date range",
        description = "Retrieves sessions for a specific teacher filtered by date range"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<List<SessionResponse>> getSessionsByTeacherAndDateRange(
        @Parameter(description = "Teacher ID") @PathVariable Long teacherId,
        @Parameter(description = "Start date (ISO format)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @Parameter(description = "End date (ISO format)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.debug("Fetching sessions for teacher {} from {} to {}", teacherId, startDate, endDate);

        List<Session> sessions = sessionService.getSessionsByTeacherAndDateRange(teacherId, startDate, endDate);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET UPCOMING SESSIONS ====================

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get upcoming sessions",
        description = "Retrieves all scheduled sessions starting from now"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    })
    public ResponseEntity<List<SessionResponse>> getUpcomingSessions() {
        log.debug("Fetching upcoming sessions");

        List<Session> sessions = sessionService.getUpcomingSessions();
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET IN PROGRESS SESSIONS ====================

    @GetMapping("/in-progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get sessions currently in progress",
        description = "Retrieves all sessions with EN_CURSO status"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    })
    public ResponseEntity<List<SessionResponse>> getInProgressSessions() {
        log.debug("Fetching in-progress sessions");

        List<Session> sessions = sessionService.getInProgressSessions();
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET SESSIONS REQUIRING ACTION ====================

    @GetMapping("/requiring-action")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get sessions requiring action",
        description = "Retrieves postponed sessions without recovery sessions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    })
    public ResponseEntity<List<SessionResponse>> getSessionsRequiringAction() {
        log.debug("Fetching sessions requiring action");

        List<Session> sessions = sessionService.getSessionsRequiringAction();
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== GET SESSIONS BY STATUS ====================

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get sessions by status",
        description = "Retrieves all sessions with a specific status"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<List<SessionResponse>> getSessionsByStatus(
        @Parameter(description = "Session status") @PathVariable String status
    ) {
        log.debug("Fetching sessions with status: {}", status);

        SessionStatus sessionStatus = SessionStatus.valueOf(status.toUpperCase());
        List<Session> sessions = sessionService.getSessionsByStatus(sessionStatus);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== START SESSION ====================

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Start a session",
        description = "Transitions a session from PROGRAMADA to EN_CURSO status. " +
                     "Can only be done within 30 minutes before scheduled start time."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session started successfully"),
        @ApiResponse(responseCode = "400", description = "Session cannot be started (wrong status or timing)"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionResponse> startSession(
        @Parameter(description = "Session ID") @PathVariable Long id,
        @Valid @RequestBody(required = false) StartSessionRequest request,
        Authentication authentication
    ) {
        log.info("Starting session {} by user {}", id, authentication.getName());

        StartSessionUseCase.StartSessionCommand command = new StartSessionUseCase.StartSessionCommand(
            id,
            request != null ? request.notes() : null
        );

        Session session = sessionService.startSession(command);
        SessionResponse response = SessionResponse.fromEntity(session);

        return ResponseEntity.ok(response);
    }

    // ==================== COMPLETE SESSION ====================

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Complete a session",
        description = "Transitions a session from EN_CURSO to COMPLETADA status. " +
                     "Topics covered must be provided."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session completed successfully"),
        @ApiResponse(responseCode = "400", description = "Session cannot be completed or topics missing"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionResponse> completeSession(
        @Parameter(description = "Session ID") @PathVariable Long id,
        @Valid @RequestBody CompleteSessionRequest request,
        Authentication authentication
    ) {
        log.info("Completing session {} by user {}", id, authentication.getName());

        CompleteSessionUseCase.CompleteSessionCommand command = new CompleteSessionUseCase.CompleteSessionCommand(
            id,
            request.topicsCovered(),
            request.notes()
        );

        Session session = sessionService.completeSession(command);
        SessionResponse response = SessionResponse.fromEntity(session);

        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE SESSION MODE ====================

    @PatchMapping("/{id}/mode")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Update session mode",
        description = "Changes the delivery mode of a session (PRESENCIAL, DUAL, ONLINE). " +
                     "Can only be done at least 2 hours before scheduled start."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mode updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid mode or cannot change in current state"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "409", description = "Classroom conflict detected")
    })
    public ResponseEntity<SessionResponse> updateSessionMode(
        @Parameter(description = "Session ID") @PathVariable Long id,
        @Valid @RequestBody UpdateSessionModeRequest request,
        Authentication authentication
    ) {
        log.info("Updating mode for session {} to {} by user {}",
            id, request.mode(), authentication.getName());

        UpdateSessionModeUseCase.UpdateSessionModeCommand command = new UpdateSessionModeUseCase.UpdateSessionModeCommand(
            id,
            request.mode(),
            request.classroom(),
            request.zoomMeetingId(),
            request.reason()
        );

        Session session = sessionService.updateSessionMode(command);
        SessionResponse response = SessionResponse.fromEntity(session);

        return ResponseEntity.ok(response);
    }

    // ==================== POSTPONE SESSION ====================

    @PostMapping("/{id}/postpone")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Postpone a session",
        description = "Marks a session as POSPUESTA. Optionally creates a recovery session if new dates are provided."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session postponed successfully"),
        @ApiResponse(responseCode = "400", description = "Session cannot be postponed or invalid data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionResponse> postponeSession(
        @Parameter(description = "Session ID") @PathVariable Long id,
        @Valid @RequestBody PostponeSessionRequest request,
        Authentication authentication
    ) {
        log.info("Postponing session {} by user {}", id, authentication.getName());

        Session resultSession;

        if (request.hasReschedulingInfo()) {
            // Postpone and create recovery session
            PostponeSessionUseCase.PostponeAndRescheduleCommand command =
                new PostponeSessionUseCase.PostponeAndRescheduleCommand(
                    id,
                    request.reason(),
                    request.newScheduledStart().toString(),
                    request.newScheduledEnd().toString(),
                    request.newZoomMeetingId(),
                    request.newClassroom(),
                    request.newZoomMeetingId()
                );

            resultSession = sessionService.postponeAndReschedule(command);
        } else {
            // Just postpone
            PostponeSessionUseCase.PostponeSessionCommand command =
                new PostponeSessionUseCase.PostponeSessionCommand(id, request.reason());

            resultSession = sessionService.postponeSession(command);
        }

        SessionResponse response = SessionResponse.fromEntity(resultSession);
        return ResponseEntity.ok(response);
    }

    // ==================== CANCEL SESSION ====================

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Cancel a session",
        description = "Marks a session as CANCELADA (terminal state). Reason is required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Session cannot be cancelled or reason missing"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SessionResponse> cancelSession(
        @Parameter(description = "Session ID") @PathVariable Long id,
        @Valid @RequestBody CancelSessionRequest request,
        Authentication authentication
    ) {
        log.info("Cancelling session {} by user {}", id, authentication.getName());

        CancelSessionUseCase.CancelSessionCommand command = new CancelSessionUseCase.CancelSessionCommand(
            id,
            request.reason()
        );

        Session session = sessionService.cancelSession(command);
        SessionResponse response = SessionResponse.fromEntity(session);

        return ResponseEntity.ok(response);
    }

    // ==================== DELETE SESSION (ADMIN ONLY) ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete a session (ADMIN only)",
        description = "Permanently deletes a session. Use with caution - prefer cancellation for normal operations."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<Void> deleteSession(
        @Parameter(description = "Session ID") @PathVariable Long id,
        Authentication authentication
    ) {
        log.warn("Deleting session {} by admin user {}", id, authentication.getName());

        // Verify session exists
        sessionService.findSessionById(id);

        // TODO: Implement actual deletion (needs repository method)
        // For now, just verify the session exists

        return ResponseEntity.noContent().build();
    }
}
