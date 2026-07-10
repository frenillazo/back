package com.acainfo.enrollment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.in.ApproveEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.ChangeCourseUseCase;
import com.acainfo.enrollment.application.port.in.EnrollStudentUseCase;
import com.acainfo.enrollment.application.port.in.GetEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.RejectEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.WithdrawEnrollmentUseCase;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ChangeCourseRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollStudentRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollmentResponse;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.RejectEnrollmentRequest;
import com.acainfo.enrollment.infrastructure.mapper.EnrollmentRestMapper;
import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ChangeCourseUseCase changeCourseUseCase;
    private final GetEnrollmentUseCase getEnrollmentUseCase;
    private final ApproveEnrollmentUseCase approveEnrollmentUseCase;
    private final RejectEnrollmentUseCase rejectEnrollmentUseCase;
    private final EnrollmentRestMapper enrollmentRestMapper;
    private final EnrollmentResponseEnricher enrollmentResponseEnricher;

    /**
     * Enroll a student in a group.
     * POST /api/enrollments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollStudentRequest request) {
        log.info("REST: Enrolling student {} in group {}", request.getStudentId(), request.getCourseId());

        Enrollment enrollment = enrollStudentUseCase.enroll(enrollmentRestMapper.toCommand(request));
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get enrollment by ID.
     * GET /api/enrollments/{id}
     * Students can only see their own enrollments; admins/teachers can see any.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.debug("REST: Getting enrollment by ID: {}", id);

        Enrollment enrollment = getEnrollmentUseCase.getById(id);

        // Students can only view their own enrollments
        if (userDetails.getUser().isStudent() && !enrollment.getStudentId().equals(userDetails.getUserId())) {
            log.warn("Student {} attempted to access enrollment {} belonging to student {}",
                    userDetails.getUserId(), id, enrollment.getStudentId());
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para ver esta inscripción");
        }

        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * Get enrollments with filters (pagination + sorting + filtering).
     * GET /api/enrollments?studentId=1&studentEmail=...&courseId=2&status=ACTIVE&...
     * Students can only see their own enrollments; admins/teachers can see any.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<EnrollmentResponse>> getEnrollmentsWithFilters(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String studentEmail,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Students can only query their own enrollments
        Long effectiveStudentId = studentId;
        if (userDetails.getUser().isStudent()) {
            effectiveStudentId = userDetails.getUserId();
            log.debug("Student {} querying own enrollments only", effectiveStudentId);
        }

        log.debug("REST: Getting enrollments with filters - studentId={}, studentEmail={}, courseId={}, status={}",
                effectiveStudentId, studentEmail, courseId, status);

        EnrollmentFilters filters = new EnrollmentFilters(
                effectiveStudentId, studentEmail, courseId, status, page, size, sortBy, sortDirection
        );

        Page<Enrollment> enrollmentsPage = getEnrollmentUseCase.findWithFilters(filters);
        Page<EnrollmentResponse> responsePage = enrollmentResponseEnricher.enrichPage(enrollmentsPage);

        return ResponseEntity.ok(PageResponse.of(responsePage));
    }

    /**
     * Get active and pending enrollments for a student.
     * Includes ACTIVE, WAITING_LIST, and PENDING_APPROVAL statuses.
     * GET /api/enrollments/student/{studentId}
     * Students can only see their own enrollments; admins can see any.
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or #studentId == authentication.principal.userId")
    public ResponseEntity<List<EnrollmentResponse>> getActiveAndPendingEnrollmentsByStudent(@PathVariable Long studentId) {
        log.debug("REST: Getting active and pending enrollments for student: {}", studentId);

        List<Enrollment> enrollments = getEnrollmentUseCase.findActiveAndPendingByStudentId(studentId);
        List<EnrollmentResponse> responses = enrollmentResponseEnricher.enrichList(enrollments);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get active enrollments for a group.
     * GET /api/enrollments/group/{courseId}
     * Only admins and teachers can see all enrollments in a group.
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsByGroup(@PathVariable Long courseId) {
        log.debug("REST: Getting active enrollments for group: {}", courseId);

        List<Enrollment> enrollments = getEnrollmentUseCase.findActiveByCourseId(courseId);
        List<EnrollmentResponse> responses = enrollmentResponseEnricher.enrichList(enrollments);

        return ResponseEntity.ok(responses);
    }

    /**
     * Withdraw from enrollment.
     * DELETE /api/enrollments/{id}
     * Students can only withdraw their own enrollments; admins can withdraw any.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("REST: Withdrawing enrollment: {}", id);

        // Get enrollment first to verify ownership
        Enrollment enrollment = getEnrollmentUseCase.getById(id);

        // Students can only withdraw their own enrollments
        if (userDetails.getUser().isStudent() && !enrollment.getStudentId().equals(userDetails.getUserId())) {
            log.warn("Student {} attempted to withdraw enrollment {} belonging to student {}",
                    userDetails.getUserId(), id, enrollment.getStudentId());
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para retirar esta inscripción");
        }

        Enrollment withdrawnEnrollment = withdrawEnrollmentUseCase.withdraw(id);
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(withdrawnEnrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * Change group for an enrollment.
     * PUT /api/enrollments/{id}/change-group
     * Students can only change their own enrollments; admins can change any.
     */
    @PutMapping("/{id}/change-course")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> changeCourse(
            @PathVariable Long id,
            @Valid @RequestBody ChangeCourseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("REST: Changing group for enrollment {} to group {}", id, request.getNewCourseId());

        // Get enrollment first to verify ownership
        Enrollment existingEnrollment = getEnrollmentUseCase.getById(id);

        // Students can only change their own enrollments
        if (userDetails.getUser().isStudent() && !existingEnrollment.getStudentId().equals(userDetails.getUserId())) {
            log.warn("Student {} attempted to change group for enrollment {} belonging to student {}",
                    userDetails.getUserId(), id, existingEnrollment.getStudentId());
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para cambiar el grupo de esta inscripción");
        }

        Enrollment enrollment = changeCourseUseCase.changeCourse(enrollmentRestMapper.toCommand(id, request));
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * Approve an enrollment request.
     * PUT /api/enrollments/{id}/approve
     * Only the group's teacher or an admin can approve.
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<EnrollmentResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("REST: Approving enrollment {} by user {}", id, userDetails.getUserId());

        Enrollment enrollment = approveEnrollmentUseCase.approve(id, userDetails.getUserId());
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * Reject an enrollment request.
     * PUT /api/enrollments/{id}/reject
     * Only the group's teacher or an admin can reject.
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<EnrollmentResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) RejectEnrollmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String reason = request != null ? request.getReason() : null;
        log.info("REST: Rejecting enrollment {} by user {} with reason: {}", id, userDetails.getUserId(), reason);

        Enrollment enrollment = rejectEnrollmentUseCase.reject(id, userDetails.getUserId(), reason);
        EnrollmentResponse response = enrollmentResponseEnricher.enrich(enrollment);

        return ResponseEntity.ok(response);
    }
}
