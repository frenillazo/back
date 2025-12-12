package com.acainfo.enrollment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.application.port.in.WaitingListUseCase;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollmentResponse;
import com.acainfo.enrollment.infrastructure.mapper.EnrollmentRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Waiting Queue operations.
 * Endpoints: /api/waiting-queue
 *
 * Security:
 * - GET: Authenticated users
 * - DELETE (leave): Owner student or ADMIN
 */
@RestController
@RequestMapping("/api/waiting-queue")
@RequiredArgsConstructor
@Slf4j
public class WaitingQueueController {

    private final WaitingListUseCase waitingListUseCase;
    private final EnrollmentRestMapper enrollmentRestMapper;

    /**
     * Get waiting list for a group (ordered by position - FIFO).
     * GET /api/waiting-queue/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<EnrollmentResponse>> getWaitingListByGroup(@PathVariable Long groupId) {
        log.debug("REST: Getting waiting list for group: {}", groupId);

        List<Enrollment> waitingList = waitingListUseCase.getWaitingListByGroupId(groupId);
        List<EnrollmentResponse> responses = enrollmentRestMapper.toResponseList(waitingList);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all waiting list positions for a student.
     * GET /api/waiting-queue/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getWaitingListByStudent(@PathVariable Long studentId) {
        log.debug("REST: Getting waiting list positions for student: {}", studentId);

        List<Enrollment> waitingList = waitingListUseCase.getWaitingListByStudentId(studentId);
        List<EnrollmentResponse> responses = enrollmentRestMapper.toResponseList(waitingList);

        return ResponseEntity.ok(responses);
    }

    /**
     * Leave waiting list (withdraw from queue).
     * DELETE /api/waiting-queue/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<EnrollmentResponse> leaveWaitingList(@PathVariable Long id) {
        log.info("REST: Leaving waiting list, enrollment: {}", id);

        Enrollment enrollment = waitingListUseCase.leaveWaitingList(id);
        EnrollmentResponse response = enrollmentRestMapper.toResponse(enrollment);

        return ResponseEntity.ok(response);
    }
}
