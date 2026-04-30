package com.acainfo.group.infrastructure.adapter.in.rest;

import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.application.port.in.CreateGroupUseCase;
import com.acainfo.group.application.port.in.DeleteGroupUseCase;
import com.acainfo.group.application.port.in.GetGroupUseCase;
import com.acainfo.group.application.port.in.UpdateGroupUseCase;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.CreateGroupRequest;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.GroupResponse;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.UpdateGroupRequest;
import com.acainfo.group.infrastructure.mapper.GroupRestMapper;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for SubjectGroup management.
 * Endpoints: /api/groups
 *
 * Security:
 * - GET (all, by id): Authenticated users
 * - POST, PUT, DELETE: ADMIN only
 *
 * All responses are enriched with related entity data (subject name, teacher name, etc.).
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final UpdateGroupUseCase updateGroupUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;
    private final GroupRestMapper groupRestMapper;
    private final GroupResponseEnricher groupResponseEnricher;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        log.info("REST: Creating group for subject: {}, teacher: {}",
                request.getSubjectId(), request.getTeacherId());

        SubjectGroup createdGroup = createGroupUseCase.create(groupRestMapper.toCommand(request));
        GroupResponse response = groupResponseEnricher.enrich(createdGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long id) {
        log.debug("REST: Getting group by ID: {}", id);

        SubjectGroup group = getGroupUseCase.getById(id);
        GroupResponse response = groupResponseEnricher.enrich(group);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/groups?subjectId=1&teacherId=2&status=OPEN&page=0&size=10&sortBy=createdAt&sortDirection=DESC
     */
    @GetMapping
    public ResponseEntity<PageResponse<GroupResponse>> getGroupsWithFilters(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("REST: Getting groups with filters - subjectId: {}, teacherId: {}, status: {}, searchTerm: {}",
                subjectId, teacherId, status, searchTerm);

        GroupFilters filters = new GroupFilters(
                subjectId,
                teacherId,
                status,
                searchTerm,
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<SubjectGroup> groupsPage = getGroupUseCase.findWithFilters(filters);
        Page<GroupResponse> responsePage = groupResponseEnricher.enrichPage(groupsPage);

        return ResponseEntity.ok(PageResponse.of(responsePage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        log.info("REST: Updating group ID: {}", id);

        SubjectGroup updatedGroup = updateGroupUseCase.update(id, groupRestMapper.toCommand(request));
        GroupResponse response = groupResponseEnricher.enrich(updatedGroup);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        log.info("REST: Deleting group ID: {}", id);

        deleteGroupUseCase.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupResponse> cancelGroup(@PathVariable Long id) {
        log.info("REST: Cancelling group ID: {}", id);

        SubjectGroup cancelledGroup = deleteGroupUseCase.cancel(id);
        GroupResponse response = groupResponseEnricher.enrich(cancelledGroup);

        return ResponseEntity.ok(response);
    }
}
