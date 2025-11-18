package acainfo.back.enrollment.infrastructure.adapters.in.rest;

import acainfo.back.enrollment.application.services.EnrollmentService;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.CancelEnrollmentRequest;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.CreateEnrollmentRequest;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollmentResponse;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.UpdateEnrollmentStatusRequest;
import acainfo.back.shared.domain.model.User;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
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
 * Provides endpoints for student enrollment operations.
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollments", description = "Student enrollment management endpoints")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Create a new enrollment",
            description = "Enrolls a student in a subject group. Students can enroll themselves, admins can enroll any student.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Enrollment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student or subject group not found"),
            @ApiResponse(responseCode = "409", description = "Student already enrolled in this group")
    })
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        log.info("Creating enrollment for student ID: {} in group ID: {}",
                request.getStudentId(), request.getSubjectGroupId());

        // Build enrollment entity from request
        Enrollment enrollment = Enrollment.builder()
                .student(User.builder().id(request.getStudentId()).build())
                .subjectGroup(SubjectGroup.builder().id(request.getSubjectGroupId()).build())
                .notes(request.getNotes())
                .build();

        Enrollment createdEnrollment = enrollmentService.createEnrollment(enrollment);
        EnrollmentResponse response = toResponse(createdEnrollment);

        log.info("Enrollment created successfully with ID: {}", createdEnrollment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== CANCEL ====================

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Cancel an enrollment",
            description = "Cancels a student's enrollment in a subject group.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Enrollment cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Enrollment cannot be cancelled"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<Void> cancelEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable Long id,
            @Valid @RequestBody CancelEnrollmentRequest request) {
        log.info("Cancelling enrollment ID: {} with reason: {}", id, request.getReason());

        enrollmentService.cancelEnrollment(id, request.getReason());

        log.info("Enrollment cancelled successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== UPDATE STATUS ====================

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update enrollment status",
            description = "Updates the status of an enrollment. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<EnrollmentResponse> updateEnrollmentStatus(
            @Parameter(description = "Enrollment ID") @PathVariable Long id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request) {
        log.info("Updating enrollment {} status to: {}", id, request.getNewStatus());

        enrollmentService.updateEnrollmentStatus(id, request.getNewStatus(), request.getReason());
        Enrollment updatedEnrollment = enrollmentService.getEnrollmentById(id);
        EnrollmentResponse response = toResponse(updatedEnrollment);

        log.info("Enrollment status updated successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    // ==================== GET ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get enrollment by ID",
            description = "Retrieves detailed information about a specific enrollment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollment found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(
            @Parameter(description = "Enrollment ID") @PathVariable Long id) {
        log.debug("Getting enrollment by ID: {}", id);

        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        EnrollmentResponse response = toResponse(enrollment);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all enrollments",
            description = "Retrieves all enrollments. Requires ADMIN or TEACHER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        log.debug("Getting all enrollments");

        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
        List<EnrollmentResponse> responses = enrollments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get enrollments by student",
            description = "Retrieves all enrollments for a specific student.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Getting enrollments for student ID: {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        List<EnrollmentResponse> responses = enrollments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get active enrollments by student",
            description = "Retrieves all active enrollments for a specific student.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active enrollments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Getting active enrollments for student ID: {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getActiveEnrollmentsByStudent(studentId);
        List<EnrollmentResponse> responses = enrollments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get enrollments by subject group",
            description = "Retrieves all enrollments for a specific subject group. Requires ADMIN or TEACHER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsBySubjectGroup(
            @Parameter(description = "Subject Group ID") @PathVariable Long groupId) {
        log.debug("Getting enrollments for subject group ID: {}", groupId);

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsBySubjectGroup(groupId);
        List<EnrollmentResponse> responses = enrollments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/group/{groupId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get active enrollments by subject group",
            description = "Retrieves all active enrollments for a specific subject group. Requires ADMIN or TEACHER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active enrollments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsBySubjectGroup(
            @Parameter(description = "Subject Group ID") @PathVariable Long groupId) {
        log.debug("Getting active enrollments for subject group ID: {}", groupId);

        List<Enrollment> enrollments = enrollmentService.getActiveEnrollmentsBySubjectGroup(groupId);
        List<EnrollmentResponse> responses = enrollments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get enrollments by status",
            description = "Retrieves all enrollments with a specific status. Requires ADMIN or TEACHER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStatus(
            @Parameter(description = "Enrollment status") @PathVariable EnrollmentStatus status) {
        log.debug("Getting enrollments by status: {}", status);

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStatus(status);
        List<EnrollmentResponse> responses = enrollments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/student/{studentId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Count active enrollments for student",
            description = "Returns the count of active enrollments for a specific student.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Long> countActiveEnrollmentsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Counting active enrollments for student ID: {}", studentId);

        long count = enrollmentService.countActiveEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(count);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Converts an Enrollment entity to an EnrollmentResponse DTO.
     */
    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName())
                .studentEmail(enrollment.getStudent().getEmail())
                .subjectGroupId(enrollment.getSubjectGroup().getId())
                .subjectCode(enrollment.getSubjectGroup().getSubject() != null ?
                        enrollment.getSubjectGroup().getSubject().getCode() : null)
                .subjectName(enrollment.getSubjectGroup().getSubject() != null ?
                        enrollment.getSubjectGroup().getSubject().getName() : null)
                .groupType(enrollment.getSubjectGroup().getType() != null ?
                        enrollment.getSubjectGroup().getType().name() : null)
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .cancellationDate(enrollment.getCancellationDate())
                .cancellationReason(enrollment.getCancellationReason())
                .onlineAttendanceAllowed(enrollment.getOnlineAttendanceAllowed())
                .notes(enrollment.getNotes())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
