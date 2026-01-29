package com.acainfo.enrollment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.in.ChangeGroupUseCase;
import com.acainfo.enrollment.application.port.in.EnrollStudentUseCase;
import com.acainfo.enrollment.application.port.in.GetEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.WithdrawEnrollmentUseCase;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ChangeGroupRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollStudentRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollmentResponse;
import com.acainfo.enrollment.infrastructure.mapper.EnrollmentRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Enrollment operations.
 * Endpoints: /api/enrollments
 *
 * Security:
 * - GET: Authenticated users
 * - POST (enroll): STUDENT or ADMIN
 * - DELETE (withdraw): Owner student or ADMIN
 * - PUT (change-group): Owner student or ADMIN
 *
 * All responses are enriched with related entity data (student name, subject name, etc.)
 * to reduce the number of API calls the frontend needs to make.
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollStudentUseCase enrollStudentUseCase;
    private final WithdrawEnrollmentUseCase withdrawEnrollmentUseCase;
    private final ChangeGroupUseCase changeGroupUseCase;
    private final GetEnrollmentUseCase getEnrollmentUseCase;
    private final EnrollmentRestMapper enrollmentRestMapper;
    private final EnrollmentResponseEnricher enrollmentResponseEnricher;

    /**
     * Enroll a student in a group.
     * POST /api/enrollments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollStudentRequest request) {
        log.info("REST: Enrolling student {} in group {}", request.getStudentId(), request.getGroupId());

        Enrollment enrollment = enrollStudentUseCase.enroll(enrollmentRestMapper.toCommand(request));
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get enrollment by ID.
     * GET /api/enrollments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(@PathVariable Long id) {
        log.debug("REST: Getting enrollment by ID: {}", id);

        Enrollment enrollment = getEnrollmentUseCase.getById(id);
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * Get enrollments with filters (pagination + sorting + filtering).
     * GET /api/enrollments?studentId=1&studentEmail=...&groupId=2&status=ACTIVE&...
     */
    @GetMapping
    public ResponseEntity<Page<EnrollmentResponse>> getEnrollmentsWithFilters(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String studentEmail,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("REST: Getting enrollments with filters - studentId={}, studentEmail={}, groupId={}, status={}",
                studentId, studentEmail, groupId, status);

        EnrollmentFilters filters = new EnrollmentFilters(
                studentId, studentEmail, groupId, status, page, size, sortBy, sortDirection
        );

        Page<Enrollment> enrollmentsPage = getEnrollmentUseCase.findWithFilters(filters);
        Page<EnrollmentResponse> responsePage = enrollmentResponseEnricher.enrichPage(enrollmentsPage);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get active enrollments for a student.
     * GET /api/enrollments/student/{studentId}
     * Students can only see their own enrollments; admins can see any.
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or #studentId == authentication.principal.userId")
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsByStudent(@PathVariable Long studentId) {
        log.debug("REST: Getting active enrollments for student: {}", studentId);

        List<Enrollment> enrollments = getEnrollmentUseCase.findActiveByStudentId(studentId);
        List<EnrollmentResponse> responses = enrollmentResponseEnricher.enrichList(enrollments);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get active enrollments for a group.
     * GET /api/enrollments/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsByGroup(@PathVariable Long groupId) {
        log.debug("REST: Getting active enrollments for group: {}", groupId);

        List<Enrollment> enrollments = getEnrollmentUseCase.findActiveByGroupId(groupId);
        List<EnrollmentResponse> responses = enrollmentResponseEnricher.enrichList(enrollments);

        return ResponseEntity.ok(responses);
    }

    /**
     * Withdraw from enrollment.
     * DELETE /api/enrollments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> withdraw(@PathVariable Long id) {
        log.info("REST: Withdrawing enrollment: {}", id);

        Enrollment enrollment = withdrawEnrollmentUseCase.withdraw(id);
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * Change group for an enrollment.
     * PUT /api/enrollments/{id}/change-group
     */
    @PutMapping("/{id}/change-group")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> changeGroup(
            @PathVariable Long id,
            @Valid @RequestBody ChangeGroupRequest request
    ) {
        log.info("REST: Changing group for enrollment {} to group {}", id, request.getNewGroupId());

        Enrollment enrollment = changeGroupUseCase.changeGroup(enrollmentRestMapper.toCommand(id, request));
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }
}
