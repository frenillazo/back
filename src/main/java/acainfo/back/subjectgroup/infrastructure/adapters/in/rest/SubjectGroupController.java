package acainfo.back.subjectgroup.infrastructure.adapters.in.rest;

import acainfo.back.subjectgroup.application.mappers.SubjectGroupDtoMapper;
import acainfo.back.subjectgroup.application.ports.in.CreateGroupUseCase;
import acainfo.back.subjectgroup.application.ports.in.DeleteGroupUseCase;
import acainfo.back.subjectgroup.application.ports.in.GetGroupUseCase;
import acainfo.back.subjectgroup.application.ports.in.UpdateGroupUseCase;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.infrastructure.adapters.in.dto.CreateSubjectGroupRequest;
import acainfo.back.subjectgroup.infrastructure.adapters.in.dto.SubjectGroupResponse;
import acainfo.back.subjectgroup.infrastructure.adapters.in.dto.UpdateSubjectGroupRequest;
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

/**
 * REST Controller for subjectGroup management.
 * Provides endpoints for CRUD operations and filtering.
 *
 * Refactored to use pure hexagonal architecture:
 * - Uses SubjectGroupDomain (pure domain model)
 * - Delegates to individual use cases
 * - Uses SubjectGroupDtoMapper for DTO conversions
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Groups", description = "SubjectGroup management endpoints")
public class SubjectGroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final UpdateGroupUseCase updateGroupUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;
    private final SubjectGroupDtoMapper groupDtoMapper;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new subjectGroup", description = "Creates a new subjectGroup for a subject. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SubjectGroup created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public ResponseEntity<SubjectGroupResponse> createGroup(@Valid @RequestBody CreateSubjectGroupRequest request) {
        log.info("Creating subjectGroup for subject ID: {}", request.getSubjectId());

        SubjectGroupDomain group = groupDtoMapper.toDomain(request);
        SubjectGroupDomain createdGroup = createGroupUseCase.createGroup(group);
        SubjectGroupResponse response = groupDtoMapper.toResponse(createdGroup);

        log.info("SubjectGroup created successfully with ID: {}", createdGroup.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a subjectGroup", description = "Updates an existing subjectGroup. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SubjectGroup updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "SubjectGroup not found")
    })
    public ResponseEntity<SubjectGroupResponse> updateGroup(
            @Parameter(description = "SubjectGroup ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectGroupRequest request) {
        log.info("Updating subjectGroup with ID: {}", id);

        SubjectGroupDomain updateData = groupDtoMapper.toDomainFromUpdate(request);
        SubjectGroupDomain updatedGroup = updateGroupUseCase.updateGroup(id, updateData);
        SubjectGroupResponse response = groupDtoMapper.toResponse(updatedGroup);

        log.info("SubjectGroup updated successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign teacher to subjectGroup", description = "Assigns a teacher to a subjectGroup. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Teacher assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid teacher"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "SubjectGroup or teacher not found")
    })
    public ResponseEntity<SubjectGroupResponse> assignTeacher(
            @Parameter(description = "SubjectGroup ID") @PathVariable Long id,
            @Parameter(description = "Teacher ID") @RequestParam Long teacherId) {
        log.info("Assigning teacher {} to subjectGroup {}", teacherId, id);

        SubjectGroupDomain updatedGroup = updateGroupUseCase.assignTeacher(id, teacherId);
        SubjectGroupResponse response = groupDtoMapper.toResponse(updatedGroup);

        log.info("Teacher assigned successfully to subjectGroup {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a subjectGroup", description = "Cancels a subjectGroup. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SubjectGroup cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "SubjectGroup not found")
    })
    public ResponseEntity<Void> cancelGroup(@Parameter(description = "SubjectGroup ID") @PathVariable Long id) {
        log.info("Cancelling subjectGroup with ID: {}", id);

        deleteGroupUseCase.cancelGroup(id);

        log.info("SubjectGroup cancelled successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GET ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get subjectGroup by ID", description = "Retrieves a subjectGroup by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SubjectGroup found"),
            @ApiResponse(responseCode = "404", description = "SubjectGroup not found")
    })
    public ResponseEntity<SubjectGroupResponse> getGroupById(@Parameter(description = "SubjectGroup ID") @PathVariable Long id) {
        log.debug("Fetching subjectGroup with ID: {}", id);

        SubjectGroupDomain group = getGroupUseCase.getGroupById(id);
        SubjectGroupResponse response = groupDtoMapper.toResponse(group);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all groups with filters", description = "Retrieves all groups with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<SubjectGroupResponse>> getAllGroups(
            @Parameter(description = "Filter by subject ID") @RequestParam(required = false) Long subjectId,
            @Parameter(description = "Filter by teacher ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "Filter by subjectGroup type") @RequestParam(required = false) GroupType type,
            @Parameter(description = "Filter by academic period") @RequestParam(required = false) AcademicPeriod period,
            @Parameter(description = "Filter by status") @RequestParam(required = false) GroupStatus status,
            @Parameter(description = "Filter groups with available places") @RequestParam(required = false) Boolean hasAvailablePlaces) {

        log.debug("Fetching subjectGroups with filters - subjectId: {}, teacherId: {}, type: {}, period: {}, status: {}, hasAvailable: {}",
                subjectId, teacherId, type, period, status, hasAvailablePlaces);

        List<SubjectGroupDomain> groups = applyFilters(subjectId, teacherId, type, period, status, hasAvailablePlaces);
        List<SubjectGroupResponse> responses = groupDtoMapper.toResponses(groups);

        log.debug("Found {} subjectGroups matching filters", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get groups by subject", description = "Retrieves all groups for a specific subject")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<SubjectGroupResponse>> getGroupsBySubject(
            @Parameter(description = "Subject ID") @PathVariable Long subjectId) {
        log.debug("Fetching subjectGroups for subject ID: {}", subjectId);

        List<SubjectGroupDomain> groups = getGroupUseCase.getGroupsBySubject(subjectId);
        List<SubjectGroupResponse> responses = groupDtoMapper.toResponses(groups);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get groups by teacher", description = "Retrieves all groups for a specific teacher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<SubjectGroupResponse>> getGroupsByTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId) {
        log.debug("Fetching subjectGroups for teacher ID: {}", teacherId);

        List<SubjectGroupDomain> groups = getGroupUseCase.getGroupsByTeacher(teacherId);
        List<SubjectGroupResponse> responses = groupDtoMapper.toResponses(groups);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    @Operation(summary = "Get groups with available places", description = "Retrieves all groups that have available places")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<SubjectGroupResponse>> getGroupsWithAvailablePlaces() {
        log.debug("Fetching subjectGroups with available places");

        List<SubjectGroupDomain> groups = getGroupUseCase.getGroupsWithAvailablePlaces();
        List<SubjectGroupResponse> responses = groupDtoMapper.toResponses(groups);

        return ResponseEntity.ok(responses);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a subjectGroup", description = "Deletes a subjectGroup. Only allowed if no students are enrolled. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SubjectGroup deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete subjectGroup with enrolled students"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "SubjectGroup not found")
    })
    public ResponseEntity<Void> deleteGroup(@Parameter(description = "SubjectGroup ID") @PathVariable Long id) {
        log.info("Deleting subjectGroup with ID: {}", id);

        deleteGroupUseCase.deleteGroup(id);

        log.info("SubjectGroup deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Applies filters using specific use case methods
     */
    private List<SubjectGroupDomain> applyFilters(Long subjectId, Long teacherId, GroupType type,
                                                   AcademicPeriod period, GroupStatus status,
                                                   Boolean hasAvailablePlaces) {

        // Priority order for filtering - most specific first
        if (hasAvailablePlaces != null && hasAvailablePlaces) {
            return getGroupUseCase.getGroupsWithAvailablePlaces();
        }

        if (subjectId != null) {
            return getGroupUseCase.getGroupsBySubject(subjectId);
        }

        if (teacherId != null) {
            return getGroupUseCase.getGroupsByTeacher(teacherId);
        }

        if (status != null) {
            return getGroupUseCase.getGroupsByStatus(status);
        }

        if (type != null) {
            return getGroupUseCase.getGroupsByType(type);
        }

        if (period != null) {
            return getGroupUseCase.getGroupsByPeriod(period);
        }

        // No filters provided, return all
        return getGroupUseCase.getAllGroups();
    }
}
