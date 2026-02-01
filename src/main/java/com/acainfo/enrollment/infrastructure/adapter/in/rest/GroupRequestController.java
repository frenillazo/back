package com.acainfo.enrollment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.application.port.in.CreateGroupRequestUseCase;
import com.acainfo.enrollment.application.port.in.GetGroupRequestUseCase;
import com.acainfo.enrollment.application.port.in.ProcessGroupRequestUseCase;
import com.acainfo.enrollment.application.port.in.SupportGroupRequestUseCase;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.enrollment.application.port.out.GroupRequestRepositoryPort;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.AddSupporterRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.CreateGroupRequestRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.GroupRequestResponse;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ProcessGroupRequestRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.SubjectInterestSummary;
import com.acainfo.enrollment.infrastructure.mapper.GroupRequestRestMapper;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.group.domain.model.GroupType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for Group Request operations.
 * Endpoints: /api/group-requests
 *
 * Security:
 * - GET: Authenticated users
 * - POST (create): STUDENT
 * - POST (support): STUDENT
 * - PUT (approve/reject): ADMIN only
 */
@RestController
@RequestMapping("/api/group-requests")
@RequiredArgsConstructor
@Slf4j
public class GroupRequestController {

    private final CreateGroupRequestUseCase createGroupRequestUseCase;
    private final SupportGroupRequestUseCase supportGroupRequestUseCase;
    private final ProcessGroupRequestUseCase processGroupRequestUseCase;
    private final GetGroupRequestUseCase getGroupRequestUseCase;
    private final GroupRequestRestMapper groupRequestRestMapper;
    private final GroupRequestResponseEnricher groupRequestResponseEnricher;
    private final GroupRequestRepositoryPort groupRequestRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;

    /**
     * Create a new group request.
     * POST /api/group-requests
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<GroupRequestResponse> createGroupRequest(
            @Valid @RequestBody CreateGroupRequestRequest request
    ) {
        log.info("REST: Creating group request for subject {} by requester {}",
                request.getSubjectId(), request.getRequesterId());

        GroupRequest groupRequest = createGroupRequestUseCase.create(groupRequestRestMapper.toCommand(request));
        GroupRequestResponse response = groupRequestResponseEnricher.enrich(groupRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get group request by ID.
     * GET /api/group-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupRequestResponse> getGroupRequestById(@PathVariable Long id) {
        log.debug("REST: Getting group request by ID: {}", id);

        GroupRequest groupRequest = getGroupRequestUseCase.getById(id);
        GroupRequestResponse response = groupRequestResponseEnricher.enrich(groupRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Get group requests with filters (pagination + sorting + filtering).
     * GET /api/group-requests?subjectId=1&requesterId=2&status=PENDING&...
     */
    @GetMapping
    public ResponseEntity<Page<GroupRequestResponse>> getGroupRequestsWithFilters(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long requesterId,
            @RequestParam(required = false) GroupType requestedGroupType,
            @RequestParam(required = false) GroupRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("REST: Getting group requests with filters - subjectId={}, status={}",
                subjectId, status);

        GroupRequestFilters filters = new GroupRequestFilters(
                subjectId, requesterId, requestedGroupType, status,
                page, size, sortBy, sortDirection
        );

        Page<GroupRequest> requestsPage = getGroupRequestUseCase.findWithFilters(filters);
        Page<GroupRequestResponse> responsePage = groupRequestResponseEnricher.enrichPage(requestsPage);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get supporters for a group request.
     * GET /api/group-requests/{id}/supporters
     */
    @GetMapping("/{id}/supporters")
    public ResponseEntity<Set<Long>> getSupporters(@PathVariable Long id) {
        log.debug("REST: Getting supporters for group request: {}", id);

        Set<Long> supporters = getGroupRequestUseCase.getSupporters(id);

        return ResponseEntity.ok(supporters);
    }

    /**
     * Add a supporter to a group request.
     * POST /api/group-requests/{id}/support
     */
    @PostMapping("/{id}/support")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<GroupRequestResponse> addSupporter(
            @PathVariable Long id,
            @Valid @RequestBody AddSupporterRequest request
    ) {
        log.info("REST: Adding supporter {} to group request {}", request.getStudentId(), id);

        GroupRequest groupRequest = supportGroupRequestUseCase.addSupporter(id, request.getStudentId());
        GroupRequestResponse response = groupRequestResponseEnricher.enrich(groupRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Remove a supporter from a group request.
     * DELETE /api/group-requests/{id}/support/{studentId}
     */
    @DeleteMapping("/{id}/support/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<GroupRequestResponse> removeSupporter(
            @PathVariable Long id,
            @PathVariable Long studentId
    ) {
        log.info("REST: Removing supporter {} from group request {}", studentId, id);

        GroupRequest groupRequest = supportGroupRequestUseCase.removeSupporter(id, studentId);
        GroupRequestResponse response = groupRequestResponseEnricher.enrich(groupRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Approve a group request (ADMIN only).
     * PUT /api/group-requests/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupRequestResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody ProcessGroupRequestRequest request
    ) {
        log.info("REST: Approving group request {} by admin {}", id, request.getAdminId());

        GroupRequest groupRequest = processGroupRequestUseCase.approve(
                groupRequestRestMapper.toCommand(id, request)
        );
        GroupRequestResponse response = groupRequestResponseEnricher.enrich(groupRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Reject a group request (ADMIN only).
     * PUT /api/group-requests/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody ProcessGroupRequestRequest request
    ) {
        log.info("REST: Rejecting group request {} by admin {}", id, request.getAdminId());

        GroupRequest groupRequest = processGroupRequestUseCase.reject(
                groupRequestRestMapper.toCommand(id, request)
        );
        GroupRequestResponse response = groupRequestResponseEnricher.enrich(groupRequest);

        return ResponseEntity.ok(response);
    }

    // ==================== "Me Interesa" Endpoints ====================

    /**
     * Get interest summary by subject (ADMIN only).
     * Returns count of interested students per subject.
     * GET /api/group-requests/interest-summary
     */
    @GetMapping("/interest-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubjectInterestSummary>> getInterestSummary() {
        log.debug("REST: Getting interest summary by subject");

        List<Object[]> counts = groupRequestRepositoryPort.countInterestedBySubject();

        // Build a map of subjectId -> count
        Map<Long, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        // Get subject details and build summaries
        List<SubjectInterestSummary> summaries = countMap.entrySet().stream()
                .map(entry -> {
                    Long subjectId = entry.getKey();
                    Integer count = entry.getValue().intValue();

                    Optional<Subject> subjectOpt = subjectRepositoryPort.findById(subjectId);

                    return SubjectInterestSummary.builder()
                            .subjectId(subjectId)
                            .subjectName(subjectOpt.map(Subject::getName).orElse("Desconocida"))
                            .subjectCode(subjectOpt.map(Subject::getCode).orElse(""))
                            .degreeName(subjectOpt.map(s -> s.getDegree() != null ? s.getDegree().getDisplayName() : "").orElse(""))
                            .interestedCount(count)
                            .build();
                })
                .sorted((a, b) -> b.getInterestedCount().compareTo(a.getInterestedCount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    /**
     * Check if student is interested in a subject.
     * GET /api/group-requests/interest/{subjectId}/student/{studentId}
     */
    @GetMapping("/interest/{subjectId}/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<Boolean> checkInterest(
            @PathVariable Long subjectId,
            @PathVariable Long studentId
    ) {
        log.debug("REST: Checking interest for subject {} by student {}", subjectId, studentId);

        boolean isInterested = groupRequestRepositoryPort
                .findPendingBySubjectIdAndRequesterId(subjectId, studentId)
                .isPresent();

        return ResponseEntity.ok(isInterested);
    }

    /**
     * Remove interest from a subject.
     * DELETE /api/group-requests/interest/{subjectId}/student/{studentId}
     */
    @DeleteMapping("/interest/{subjectId}/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<Void> removeInterest(
            @PathVariable Long subjectId,
            @PathVariable Long studentId
    ) {
        log.info("REST: Removing interest for subject {} by student {}", subjectId, studentId);

        Optional<GroupRequest> requestOpt = groupRequestRepositoryPort
                .findPendingBySubjectIdAndRequesterId(subjectId, studentId);

        if (requestOpt.isPresent()) {
            groupRequestRepositoryPort.delete(requestOpt.get().getId());
        }

        return ResponseEntity.noContent().build();
    }
}
