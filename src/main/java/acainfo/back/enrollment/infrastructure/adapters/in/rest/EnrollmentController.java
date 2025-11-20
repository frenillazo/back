package acainfo.back.enrollment.infrastructure.adapters.in.rest;

import acainfo.back.enrollment.application.services.EnrollmentService;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.ChangeGroupRequest;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollStudentRequest;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for enrollment management.
 * Provides endpoints for student enrollments, withdrawals, and queries.
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollments", description = "Enrollment management endpoints")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // ==================== ENROLL ====================

    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Enroll a student in a group",
        description = "Enrolls a student in a subject group. Validates group availability and student status. " +
                      "If group is full, enrollment goes to waiting queue (EN_ESPERA)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student enrolled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Student or group not found"),
        @ApiResponse(responseCode = "409", description = "Student already enrolled in this group")
    })
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollStudentRequest request) {
        log.info("Enrolling student {} in group {}", request.getStudentId(), request.getGroupId());

        Enrollment enrollment = enrollmentService.enrollStudent(request.getStudentId(), request.getGroupId());
        EnrollmentResponse response = EnrollmentResponse.fromEntity(enrollment);

        log.info("Student {} enrolled successfully in group {} with status {}",
            request.getStudentId(), request.getGroupId(), enrollment.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/can-enroll")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Check if student can enroll",
        description = "Validates if a student can enroll in a group without actually enrolling"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Validation result returned"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Boolean> canEnroll(
            @Parameter(description = "Student ID") @RequestParam Long studentId,
            @Parameter(description = "Group ID") @RequestParam Long groupId) {
        log.debug("Checking if student {} can enroll in group {}", studentId, groupId);

        boolean canEnroll = enrollmentService.canEnroll(studentId, groupId);
        return ResponseEntity.ok(canEnroll);
    }

    // ==================== WITHDRAW ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Withdraw from enrollment",
        description = "Withdraws a student from a group. Frees physical space if enrollment was presential."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Enrollment withdrawn successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<Void> withdrawEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable Long id,
            @Parameter(description = "Withdrawal reason") @RequestParam(required = false) String reason) {
        log.info("Withdrawing enrollment {}", id);

        enrollmentService.withdrawEnrollment(id, reason);

        log.info("Enrollment {} withdrawn successfully", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CHANGE GROUP ====================

    @PutMapping("/{id}/change-group")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Change to a different group",
        description = "Changes student's enrollment to a different group of the same subject. " +
                      "Withdraws from current group and enrolls in new group."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or different subject"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Enrollment or new group not found")
    })
    public ResponseEntity<EnrollmentResponse> changeGroup(
            @Parameter(description = "Current enrollment ID") @PathVariable Long id,
            @Valid @RequestBody ChangeGroupRequest request) {
        log.info("Changing enrollment {} to new group {}", id, request.getNewGroupId());

        Enrollment newEnrollment = enrollmentService.changeGroup(id, request.getNewGroupId());
        EnrollmentResponse response = EnrollmentResponse.fromEntity(newEnrollment);

        log.info("Enrollment {} changed successfully to group {}", id, request.getNewGroupId());
        return ResponseEntity.ok(response);
    }

    // ==================== GET ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get enrollment by ID", description = "Retrieves detailed information about an enrollment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollment found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(
            @Parameter(description = "Enrollment ID") @PathVariable Long id) {
        log.debug("Fetching enrollment by ID: {}", id);

        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        EnrollmentResponse response = EnrollmentResponse.fromEntity(enrollment);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get all enrollments for a student",
        description = "Retrieves all enrollments (active, withdrawn, waiting) for a specific student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getStudentEnrollments(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Fetching all enrollments for student: {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getAllEnrollmentsByStudent(studentId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/active")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get active enrollments for a student",
        description = "Retrieves only active enrollments for a specific student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active enrollments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getActiveStudentEnrollments(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Fetching active enrollments for student: {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getActiveEnrollmentsByStudent(studentId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get all enrollments for a group",
        description = "Retrieves all enrollments (active, withdrawn, waiting) for a specific group. " +
                      "Only teachers and admins can view this."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getGroupEnrollments(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        log.debug("Fetching all enrollments for group: {}", groupId);

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByGroup(groupId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/active")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get active enrollments for a group",
        description = "Retrieves only active enrollments for a specific group. " +
                      "Useful for teachers to see their current student list."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active enrollments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getActiveGroupEnrollments(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        log.debug("Fetching active enrollments for group: {}", groupId);

        List<Enrollment> enrollments = enrollmentService.getActiveEnrollmentsByGroup(groupId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Check if student is enrolled in a group",
        description = "Verifies if a student has an active enrollment in a specific group"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check result returned"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Boolean> isStudentEnrolled(
            @Parameter(description = "Student ID") @RequestParam Long studentId,
            @Parameter(description = "Group ID") @RequestParam Long groupId) {
        log.debug("Checking if student {} is enrolled in group {}", studentId, groupId);

        boolean isEnrolled = enrollmentService.isStudentEnrolled(studentId, groupId);
        return ResponseEntity.ok(isEnrolled);
    }
}
