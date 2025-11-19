package acainfo.back.attendance.infrastructure.adapters.in.rest;

import acainfo.back.attendance.application.ports.in.*;
import acainfo.back.attendance.application.services.AttendanceService;
import acainfo.back.attendance.domain.model.Attendance;
import acainfo.back.attendance.infrastructure.adapters.in.dto.*;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for attendance management.
 * Provides endpoints for attendance registration, queries, updates, and statistics.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance", description = "Attendance management API - Registration, queries, and statistics")
@SecurityRequirement(name = "bearer-jwt")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ==================== REGISTER ATTENDANCE ====================

    @PostMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Register attendance for a session",
        description = "Register individual student attendance for a specific session. " +
                     "Only teachers and admins can register attendance. " +
                     "Session must be in COMPLETADA status."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Attendance registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or session not completed"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "409", description = "Attendance already registered for this student")
    })
    public ResponseEntity<AttendanceResponse> registerAttendance(
        @Parameter(description = "Session ID") @PathVariable Long sessionId,
        @Valid @RequestBody RegisterAttendanceRequest request,
        Authentication authentication
    ) {
        log.info("Registering attendance for session {} by user {}",
            sessionId, authentication.getName());

        // Extract user ID from authentication (assuming it's stored in name or details)
        Long recordedById = extractUserId(authentication);

        RegisterAttendanceUseCase.RegisterAttendanceCommand command =
            new RegisterAttendanceUseCase.RegisterAttendanceCommand(
                sessionId,
                request.studentId(),
                request.status(),
                request.minutesLate(),
                request.notes(),
                recordedById
            );

        Attendance attendance = attendanceService.registerAttendance(command);
        AttendanceResponse response = AttendanceResponse.fromEntity(attendance);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sessions/{sessionId}/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Register bulk attendance for a session",
        description = "Register attendance for multiple students in a single request. " +
                     "Typically used by teachers to register attendance for an entire class. " +
                     "Duplicate entries will be skipped automatically."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bulk attendance registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or session not completed"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<List<AttendanceResponse>> registerBulkAttendance(
        @Parameter(description = "Session ID") @PathVariable Long sessionId,
        @Valid @RequestBody BulkAttendanceRequest request,
        Authentication authentication
    ) {
        log.info("Registering bulk attendance for session {} ({} students) by user {}",
            sessionId, request.attendances().size(), authentication.getName());

        Long recordedById = extractUserId(authentication);

        // Convert DTO to use case command
        List<RegisterAttendanceUseCase.StudentAttendanceData> attendancesData = request.attendances().stream()
            .map(dto -> new RegisterAttendanceUseCase.StudentAttendanceData(
                dto.studentId(),
                dto.status(),
                dto.minutesLate(),
                dto.notes()
            ))
            .toList();

        RegisterAttendanceUseCase.RegisterBulkAttendanceCommand command =
            new RegisterAttendanceUseCase.RegisterBulkAttendanceCommand(
                sessionId,
                attendancesData,
                recordedById
            );

        List<Attendance> attendances = attendanceService.registerBulkAttendance(command);
        List<AttendanceResponse> responses = attendances.stream()
            .map(AttendanceResponse::fromEntity)
            .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // ==================== GET ATTENDANCE ====================

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(
        summary = "Get attendance for a session",
        description = "Retrieve all attendance records for a specific session."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<List<AttendanceResponse>> getAttendanceBySession(
        @Parameter(description = "Session ID") @PathVariable Long sessionId
    ) {
        log.debug("Retrieving attendance for session {}", sessionId);

        List<Attendance> attendances = attendanceService.getAttendanceBySession(sessionId);
        List<AttendanceResponse> responses = attendances.stream()
            .map(AttendanceResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @Operation(
        summary = "Get attendance history for a student",
        description = "Retrieve complete attendance history for a specific student. " +
                     "Students can only access their own history."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance history retrieved successfully")
    })
    public ResponseEntity<List<AttendanceResponse>> getAttendanceHistoryByStudent(
        @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        log.debug("Retrieving attendance history for student {}", studentId);

        List<Attendance> attendances = attendanceService.getAttendanceHistoryByStudent(studentId);
        List<AttendanceResponse> responses = attendances.stream()
            .map(AttendanceResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/students/{studentId}/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @Operation(
        summary = "Get attendance history for a student within date range",
        description = "Retrieve attendance history for a student filtered by date range. " +
                     "Useful for generating period reports."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance history retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<List<AttendanceResponse>> getAttendanceHistoryByStudentAndDateRange(
        @Parameter(description = "Student ID") @PathVariable Long studentId,
        @Parameter(description = "Start date (yyyy-MM-dd)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date (yyyy-MM-dd)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.debug("Retrieving attendance history for student {} from {} to {}",
            studentId, startDate, endDate);

        GetAttendanceUseCase.AttendanceHistoryQuery query =
            new GetAttendanceUseCase.AttendanceHistoryQuery(studentId, startDate, endDate);

        List<Attendance> attendances = attendanceService.getAttendanceHistoryByStudentAndDateRange(query);
        List<AttendanceResponse> responses = attendances.stream()
            .map(AttendanceResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get all attendance for a group",
        description = "Retrieve all attendance records for a specific subject group."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully")
    })
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByGroup(
        @Parameter(description = "Subject Group ID") @PathVariable Long groupId
    ) {
        log.debug("Retrieving attendance for group {}", groupId);

        List<Attendance> attendances = attendanceService.getAttendanceByGroup(groupId);
        List<AttendanceResponse> responses = attendances.stream()
            .map(AttendanceResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{attendanceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(
        summary = "Get attendance by ID",
        description = "Retrieve a specific attendance record by its ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance record retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found")
    })
    public ResponseEntity<AttendanceResponse> getAttendanceById(
        @Parameter(description = "Attendance ID") @PathVariable Long attendanceId
    ) {
        log.debug("Retrieving attendance by ID: {}", attendanceId);

        Attendance attendance = attendanceService.getAttendanceById(attendanceId);
        AttendanceResponse response = AttendanceResponse.fromEntity(attendance);

        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ATTENDANCE ====================

    @PutMapping("/{attendanceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Update attendance status",
        description = "Update the status of an existing attendance record. " +
                     "Can only be modified within 7 days of recording."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status or modification not allowed"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found")
    })
    public ResponseEntity<AttendanceResponse> updateAttendanceStatus(
        @Parameter(description = "Attendance ID") @PathVariable Long attendanceId,
        @Valid @RequestBody UpdateAttendanceRequest request,
        Authentication authentication
    ) {
        log.info("Updating attendance {} by user {}", attendanceId, authentication.getName());

        Long updatedById = extractUserId(authentication);

        UpdateAttendanceUseCase.UpdateAttendanceStatusCommand command =
            new UpdateAttendanceUseCase.UpdateAttendanceStatusCommand(
                attendanceId,
                request.newStatus(),
                request.notes(),
                updatedById
            );

        Attendance attendance = attendanceService.updateAttendanceStatus(command);
        AttendanceResponse response = AttendanceResponse.fromEntity(attendance);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{attendanceId}/justify")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Justify an absence",
        description = "Justify an absence with documentation/reason. " +
                     "Can only justify attendance with AUSENTE status. " +
                     "Changes status to JUSTIFICADO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Absence justified successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot justify current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found")
    })
    public ResponseEntity<AttendanceResponse> justifyAbsence(
        @Parameter(description = "Attendance ID") @PathVariable Long attendanceId,
        @Valid @RequestBody JustifyAbsenceRequest request,
        Authentication authentication
    ) {
        log.info("Justifying absence for attendance {} by user {}",
            attendanceId, authentication.getName());

        Long justifiedById = extractUserId(authentication);

        UpdateAttendanceUseCase.JustifyAbsenceCommand command =
            new UpdateAttendanceUseCase.JustifyAbsenceCommand(
                attendanceId,
                request.justificationReason(),
                justifiedById
            );

        Attendance attendance = attendanceService.justifyAbsence(command);
        AttendanceResponse response = AttendanceResponse.fromEntity(attendance);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{attendanceId}/late")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Mark attendance as late",
        description = "Mark a student as late with specified minutes. " +
                     "Changes status to TARDANZA."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Marked as late successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid minutes or modification not allowed"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found")
    })
    public ResponseEntity<AttendanceResponse> markAsLate(
        @Parameter(description = "Attendance ID") @PathVariable Long attendanceId,
        @Valid @RequestBody MarkAsLateRequest request,
        Authentication authentication
    ) {
        log.info("Marking attendance {} as late ({} minutes) by user {}",
            attendanceId, request.minutesLate(), authentication.getName());

        Long updatedById = extractUserId(authentication);

        UpdateAttendanceUseCase.MarkAsLateCommand command =
            new UpdateAttendanceUseCase.MarkAsLateCommand(
                attendanceId,
                request.minutesLate(),
                request.notes(),
                updatedById
            );

        Attendance attendance = attendanceService.markAsLate(command);
        AttendanceResponse response = AttendanceResponse.fromEntity(attendance);

        return ResponseEntity.ok(response);
    }

    // ==================== STATISTICS ====================

    @GetMapping("/students/{studentId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @Operation(
        summary = "Get attendance statistics for a student",
        description = "Retrieve detailed attendance statistics for a student including " +
                     "attendance rate, absences, tardiness, and compliance with minimum requirements."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<AttendanceStatisticsResponse.StudentStats> getStudentAttendanceStats(
        @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        log.debug("Retrieving attendance statistics for student {}", studentId);

        GetAttendanceStatisticsUseCase.StudentAttendanceStats stats =
            attendanceService.getStudentAttendanceStats(studentId);

        AttendanceStatisticsResponse.StudentStats response =
            AttendanceStatisticsResponse.StudentStats.fromUseCaseResult(stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/statistics/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @Operation(
        summary = "Get attendance statistics for a student within date range",
        description = "Retrieve attendance statistics for a student filtered by date range."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<AttendanceStatisticsResponse.StudentStats> getStudentAttendanceStatsByDateRange(
        @Parameter(description = "Student ID") @PathVariable Long studentId,
        @Parameter(description = "Start date (yyyy-MM-dd)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date (yyyy-MM-dd)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.debug("Retrieving attendance statistics for student {} from {} to {}",
            studentId, startDate, endDate);

        GetAttendanceStatisticsUseCase.AttendanceStatsQuery query =
            new GetAttendanceStatisticsUseCase.AttendanceStatsQuery(studentId, startDate, endDate);

        GetAttendanceStatisticsUseCase.StudentAttendanceStats stats =
            attendanceService.getStudentAttendanceStatsByDateRange(query);

        AttendanceStatisticsResponse.StudentStats response =
            AttendanceStatisticsResponse.StudentStats.fromUseCaseResult(stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get attendance statistics for a group",
        description = "Retrieve detailed attendance statistics for an entire subject group " +
                     "including average attendance rate, sessions with perfect/low attendance, etc."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<AttendanceStatisticsResponse.GroupStats> getGroupAttendanceStats(
        @Parameter(description = "Subject Group ID") @PathVariable Long groupId
    ) {
        log.debug("Retrieving attendance statistics for group {}", groupId);

        GetAttendanceStatisticsUseCase.GroupAttendanceStats stats =
            attendanceService.getGroupAttendanceStats(groupId);

        AttendanceStatisticsResponse.GroupStats response =
            AttendanceStatisticsResponse.GroupStats.fromUseCaseResult(stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{sessionId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get attendance statistics for a session",
        description = "Retrieve attendance statistics for a specific session including " +
                     "attendance rate and breakdown by status."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<AttendanceStatisticsResponse.SessionStats> getSessionAttendanceStats(
        @Parameter(description = "Session ID") @PathVariable Long sessionId
    ) {
        log.debug("Retrieving attendance statistics for session {}", sessionId);

        GetAttendanceStatisticsUseCase.SessionAttendanceStats stats =
            attendanceService.getSessionAttendanceStats(sessionId);

        AttendanceStatisticsResponse.SessionStats response =
            AttendanceStatisticsResponse.SessionStats.fromUseCaseResult(stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/groups/{groupId}/check-requirements")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @Operation(
        summary = "Check if student meets attendance requirements",
        description = "Verify if a student meets minimum attendance requirements for a group. " +
                     "Used for access control to materials or exams."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Requirement check completed")
    })
    public ResponseEntity<Boolean> checkAttendanceRequirements(
        @Parameter(description = "Student ID") @PathVariable Long studentId,
        @Parameter(description = "Subject Group ID") @PathVariable Long groupId,
        @Parameter(description = "Minimum required percentage (default: 75.0)")
        @RequestParam(defaultValue = "75.0") Double minimumPercentage
    ) {
        log.debug("Checking if student {} meets {}% attendance requirement in group {}",
            studentId, minimumPercentage, groupId);

        boolean meets = attendanceService.meetsAttendanceRequirements(
            studentId, groupId, minimumPercentage
        );

        return ResponseEntity.ok(meets);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Extracts user ID from authentication object.
     * This is a placeholder - actual implementation depends on your security setup.
     *
     * @param authentication the authentication object
     * @return the user ID
     */
    private Long extractUserId(Authentication authentication) {
        // TODO: Implement based on your security configuration
        // This could be from authentication.getPrincipal(), authentication.getDetails(), etc.
        // For now, returning a placeholder
        return 1L; // Replace with actual implementation
    }
}
