package com.acainfo.student.infrastructure.adapter.in.rest;

import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.student.application.dto.StudentOverviewResponse;
import com.acainfo.student.application.service.StudentOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Student dashboard operations.
 * Provides aggregated overview endpoints for students.
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Dashboard", description = "Student dashboard and overview endpoints")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private static final int DEFAULT_UPCOMING_SESSIONS = 5;
    private static final int MAX_UPCOMING_SESSIONS = 20;

    private final StudentOverviewService studentOverviewService;

    /**
     * Get overview for the authenticated student.
     * GET /api/student/overview
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Get student overview",
            description = "Returns aggregated dashboard data for the authenticated student including " +
                    "active enrollments, upcoming sessions, and payment status"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StudentOverviewResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not a student")
    })
    public ResponseEntity<StudentOverviewResponse> getOverview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Maximum number of upcoming sessions to return (default: 5, max: 20)")
            @RequestParam(defaultValue = "5") int upcomingSessionsLimit
    ) {
        Long studentId = userDetails.getUserId();
        log.info("REST: Getting overview for authenticated student: {}", studentId);

        int limit = Math.min(Math.max(upcomingSessionsLimit, 1), MAX_UPCOMING_SESSIONS);
        StudentOverviewResponse response = studentOverviewService.getOverview(studentId, limit);

        return ResponseEntity.ok(response);
    }

    /**
     * Get overview for any student (admin access).
     * GET /api/student/{studentId}/overview
     */
    @GetMapping("/{studentId}/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get student overview (admin)",
            description = "Returns aggregated dashboard data for any student. Admin access only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StudentOverviewResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not an admin"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<StudentOverviewResponse> getOverviewByStudentId(
            @Parameter(description = "Student ID")
            @PathVariable Long studentId,
            @Parameter(description = "Maximum number of upcoming sessions to return (default: 5, max: 20)")
            @RequestParam(defaultValue = "5") int upcomingSessionsLimit
    ) {
        log.info("REST: Admin getting overview for student: {}", studentId);

        int limit = Math.min(Math.max(upcomingSessionsLimit, 1), MAX_UPCOMING_SESSIONS);
        StudentOverviewResponse response = studentOverviewService.getOverview(studentId, limit);

        return ResponseEntity.ok(response);
    }
}
