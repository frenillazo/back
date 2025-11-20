package acainfo.back.enrollment.infrastructure.adapters.in.rest;

import acainfo.back.enrollment.application.services.GroupRequestService;
import acainfo.back.enrollment.domain.model.GroupRequest;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.CreateGroupRequestRequest;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.GroupRequestResponse;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.RejectGroupRequestRequest;
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
 * REST Controller for group request management.
 * Allows students to request creation of new groups and admins to approve/reject them.
 */
@RestController
@RequestMapping("/api/group-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Group Requests", description = "Group request management endpoints")
public class GroupRequestController {

    private final GroupRequestService groupRequestService;

    // ==================== CREATE REQUEST ====================

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Create a group request",
        description = "Creates a new request to create a group for a subject. " +
                      "The requester is automatically added as the first supporter. " +
                      "Requires minimum 8 supporters for admin consideration."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Group request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or duplicate pending request exists"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public ResponseEntity<GroupRequestResponse> createGroupRequest(@Valid @RequestBody CreateGroupRequestRequest request) {
        log.info("Creating group request for subject {} by student {}",
            request.getSubjectId(), request.getRequesterId());

        GroupRequest groupRequest = groupRequestService.createGroupRequest(
            request.getSubjectId(),
            request.getRequesterId(),
            request.getComments()
        );

        GroupRequestResponse response = GroupRequestResponse.fromEntity(groupRequest);

        log.info("Group request created successfully with ID: {}", groupRequest.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== SUPPORT/UNSUPPORT ====================

    @PostMapping("/{id}/support")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Support a group request",
        description = "Adds the student as a supporter of the request. " +
                      "When minimum supporters (8) is reached, admin is notified."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Support added successfully"),
        @ApiResponse(responseCode = "400", description = "Already supporting or request is not pending"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<GroupRequestResponse> supportRequest(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @Parameter(description = "Student ID") @RequestParam Long studentId) {
        log.info("Student {} supporting group request {}", studentId, id);

        GroupRequest groupRequest = groupRequestService.supportRequest(id, studentId);
        GroupRequestResponse response = GroupRequestResponse.fromEntity(groupRequest);

        log.info("Support added successfully. Total supporters: {}", groupRequest.getSupportersCount());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/support")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Remove support from a group request",
        description = "Removes the student's support from the request. " +
                      "The requester cannot remove their own support."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Support removed successfully"),
        @ApiResponse(responseCode = "400", description = "Not supporting or is the requester"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<GroupRequestResponse> unsupportRequest(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @Parameter(description = "Student ID") @RequestParam Long studentId) {
        log.info("Student {} removing support from group request {}", studentId, id);

        GroupRequest groupRequest = groupRequestService.unsupportRequest(id, studentId);
        GroupRequestResponse response = GroupRequestResponse.fromEntity(groupRequest);

        log.info("Support removed successfully. Total supporters: {}", groupRequest.getSupportersCount());
        return ResponseEntity.ok(response);
    }

    // ==================== APPROVE/REJECT (ADMIN ONLY) ====================

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Approve a group request",
        description = "Approves the request and triggers creation of a new subject group. " +
                      "Requires minimum 8 supporters. Only admins can approve."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request approved successfully"),
        @ApiResponse(responseCode = "400", description = "Insufficient supporters or request not pending"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<GroupRequestResponse> approveRequest(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @Parameter(description = "Admin ID") @RequestParam Long adminId) {
        log.info("Admin {} approving group request {}", adminId, id);

        GroupRequest groupRequest = groupRequestService.approveRequest(id, adminId);
        GroupRequestResponse response = GroupRequestResponse.fromEntity(groupRequest);

        log.info("Group request {} approved successfully", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Reject a group request",
        description = "Rejects the request with a reason. Only admins can reject."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Missing reason or request not pending"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<GroupRequestResponse> rejectRequest(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @Parameter(description = "Admin ID") @RequestParam Long adminId,
            @Valid @RequestBody RejectGroupRequestRequest request) {
        log.info("Admin {} rejecting group request {}", adminId, id);

        GroupRequest groupRequest = groupRequestService.rejectRequest(id, adminId, request.getReason());
        GroupRequestResponse response = GroupRequestResponse.fromEntity(groupRequest);

        log.info("Group request {} rejected. Reason: {}", id, request.getReason());
        return ResponseEntity.ok(response);
    }

    // ==================== GET ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Get group request by ID", description = "Retrieves detailed information about a group request")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<GroupRequestResponse> getRequestById(
            @Parameter(description = "Request ID") @PathVariable Long id) {
        log.debug("Fetching group request by ID: {}", id);

        GroupRequest groupRequest = groupRequestService.getRequestById(id);
        GroupRequestResponse response = GroupRequestResponse.fromEntity(groupRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @Operation(
        summary = "Get all pending group requests",
        description = "Retrieves all requests waiting for approval or more supporters"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<GroupRequestResponse>> getAllPendingRequests() {
        log.debug("Fetching all pending group requests");

        List<GroupRequest> requests = groupRequestService.getAllPendingRequests();
        List<GroupRequestResponse> response = requests.stream()
                .map(GroupRequestResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subjects/{subjectId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Get group requests for a subject",
        description = "Retrieves all requests (pending, approved, rejected) for a specific subject"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Requests retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<GroupRequestResponse>> getRequestsBySubject(
            @Parameter(description = "Subject ID") @PathVariable Long subjectId) {
        log.debug("Fetching group requests for subject: {}", subjectId);

        List<GroupRequest> requests = groupRequestService.getRequestsBySubject(subjectId);
        List<GroupRequestResponse> response = requests.stream()
                .map(GroupRequestResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/created")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Get requests created by a student",
        description = "Retrieves all requests initiated by a specific student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Requests retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<GroupRequestResponse>> getRequestsByRequester(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Fetching group requests created by student: {}", studentId);

        List<GroupRequest> requests = groupRequestService.getRequestsByRequester(studentId);
        List<GroupRequestResponse> response = requests.stream()
                .map(GroupRequestResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/supported")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Get requests supported by a student",
        description = "Retrieves all requests where the student is a supporter"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Requests retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<GroupRequestResponse>> getRequestsSupportedByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        log.debug("Fetching group requests supported by student: {}", studentId);

        List<GroupRequest> requests = groupRequestService.getRequestsSupportedByStudent(studentId);
        List<GroupRequestResponse> response = requests.stream()
                .map(GroupRequestResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/check-support")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "Check if student supports a request",
        description = "Verifies if a student is a supporter of a specific request"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check result returned"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Boolean> isStudentSupporter(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @Parameter(description = "Student ID") @RequestParam Long studentId) {
        log.debug("Checking if student {} supports request {}", studentId, id);

        boolean isSupporter = groupRequestService.isStudentSupporter(id, studentId);
        return ResponseEntity.ok(isSupporter);
    }
}
