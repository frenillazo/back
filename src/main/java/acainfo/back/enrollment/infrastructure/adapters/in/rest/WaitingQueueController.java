package acainfo.back.enrollment.infrastructure.adapters.in.rest;

import acainfo.back.enrollment.application.services.WaitingQueueService;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for waiting queue management.
 * Allows admins and teachers to view and manage the waiting queue for groups.
 * Students can check their queue position.
 */
@RestController
@RequestMapping("/api/waiting-queue")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Waiting Queue", description = "Waiting queue management endpoints")
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    // ==================== GET WAITING QUEUE ====================

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get waiting queue for a group",
        description = "Retrieves all students waiting for a group, ordered by enrollment date (FIFO). " +
                      "Only teachers and admins can view this."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Waiting queue retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<List<EnrollmentResponse>> getWaitingQueue(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        log.debug("Getting waiting queue for group: {}", groupId);

        List<Enrollment> waitingQueue = waitingQueueService.getWaitingQueue(groupId);
        List<EnrollmentResponse> response = waitingQueue.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());

        log.debug("Group {} has {} students waiting", groupId, response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/count")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get waiting count for a group",
        description = "Returns the total number of students waiting for a group"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Long> getWaitingCount(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        log.debug("Getting waiting count for group: {}", groupId);

        long count = waitingQueueService.getWaitingCount(groupId);
        return ResponseEntity.ok(count);
    }

    // ==================== CHECK QUEUE POSITION ====================

    @GetMapping("/enrollments/{enrollmentId}/position")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get queue position for an enrollment",
        description = "Returns the position of a student in the waiting queue (1-based). " +
                      "Returns -1 if not in waiting queue."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Position retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<Integer> getQueuePosition(
            @Parameter(description = "Enrollment ID") @PathVariable Long enrollmentId) {
        log.debug("Getting queue position for enrollment: {}", enrollmentId);

        int position = waitingQueueService.getQueuePosition(enrollmentId);
        return ResponseEntity.ok(position);
    }

    // ==================== PROCESS QUEUE (ADMIN ONLY) ====================

    @PostMapping("/groups/{groupId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Manually process waiting queue",
        description = "Manually processes the entire waiting queue for a group. " +
                      "This activates all possible students from the queue based on available places. " +
                      "Useful after increasing group capacity. Only admins can use this."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Queue processed successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<ProcessQueueResponse> processQueue(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        log.info("Admin manually processing waiting queue for group: {}", groupId);

        int activatedCount = waitingQueueService.processEntireQueue(groupId);

        ProcessQueueResponse response = ProcessQueueResponse.builder()
                .groupId(groupId)
                .studentsActivated(activatedCount)
                .message(String.format("%d student(s) activated from waiting queue", activatedCount))
                .build();

        log.info("Waiting queue processed for group {}. Students activated: {}", groupId, activatedCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Response DTO for process queue operation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessQueueResponse {
        private Long groupId;
        private Integer studentsActivated;
        private String message;
    }
}
