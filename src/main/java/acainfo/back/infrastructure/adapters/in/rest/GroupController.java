package acainfo.back.infrastructure.adapters.in.rest;

import acainfo.back.application.services.GroupService;
import acainfo.back.domain.model.*;
import acainfo.back.domain.validation.GroupSpecifications;
import acainfo.back.infrastructure.adapters.in.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for group management.
 * Provides endpoints for CRUD operations and advanced filtering.
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Groups", description = "Group management endpoints")
public class GroupController {

    private final GroupService groupService;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new group", description = "Creates a new group for a subject. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        log.info("Creating group for subject ID: {}", request.getSubjectId());

        // Build group entity from request
        Group group = Group.builder()
                .subject(Subject.builder().id(request.getSubjectId()).build())
                .type(request.getType())
                .period(request.getPeriod())
                .classroom(request.getClassroom())
                .build();

        // Set teacher if provided
        if (request.getTeacherId() != null) {
            group.setTeacher(User.builder().id(request.getTeacherId()).build());
        }

        Group createdGroup = groupService.createGroup(group);
        GroupResponse response = GroupResponse.fromEntity(createdGroup);

        log.info("Group created successfully with ID: {}", createdGroup.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a group", description = "Updates an existing group. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<GroupResponse> updateGroup(
            @Parameter(description = "Group ID") @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request) {
        log.info("Updating group with ID: {}", id);

        Group updatedGroup = groupService.updateGroup(id, request.getType(), request.getPeriod(),
                request.getStatus(), request.getClassroom());
        GroupResponse response = GroupResponse.fromEntity(updatedGroup);

        log.info("Group updated successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign teacher to group", description = "Assigns a teacher to a group. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Teacher assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid teacher"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Group or teacher not found")
    })
    public ResponseEntity<GroupResponse> assignTeacher(
            @Parameter(description = "Group ID") @PathVariable Long id,
            @Parameter(description = "Teacher ID") @RequestParam Long teacherId) {
        log.info("Assigning teacher {} to group {}", teacherId, id);

        Group updatedGroup = groupService.assignTeacher(id, teacherId);
        GroupResponse response = GroupResponse.fromEntity(updatedGroup);

        log.info("Teacher assigned successfully to group {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a group", description = "Cancels a group. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Group cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Void> cancelGroup(@Parameter(description = "Group ID") @PathVariable Long id) {
        log.info("Cancelling group with ID: {}", id);

        groupService.cancelGroup(id);

        log.info("Group cancelled successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GET ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieves a group by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group found"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<GroupResponse> getGroupById(@Parameter(description = "Group ID") @PathVariable Long id) {
        log.debug("Fetching group with ID: {}", id);

        Group group = groupService.getGroupById(id);
        GroupResponse response = GroupResponse.fromEntity(group);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all groups with filters", description = "Retrieves all groups with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<GroupResponse>> getAllGroups(
            @Parameter(description = "Filter by subject ID") @RequestParam(required = false) Long subjectId,
            @Parameter(description = "Filter by teacher ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "Filter by group type") @RequestParam(required = false) GroupType type,
            @Parameter(description = "Filter by academic period") @RequestParam(required = false) AcademicPeriod period,
            @Parameter(description = "Filter by status") @RequestParam(required = false) GroupStatus status,
            @Parameter(description = "Filter by classroom") @RequestParam(required = false) Classroom classroom,
            @Parameter(description = "Filter groups with available places") @RequestParam(required = false) Boolean hasAvailablePlaces,
            @Parameter(description = "Filter by year") @RequestParam(required = false) Integer year) {

        log.debug("Fetching groups with filters - subjectId: {}, teacherId: {}, type: {}, period: {}, " +
                        "status: {}, classroom: {}, hasAvailable: {}, year: {}",
                subjectId, teacherId, type, period, status, classroom, hasAvailablePlaces, year);

        List<Group> groups = applyFilters(subjectId, teacherId, type, period, status, classroom, hasAvailablePlaces, year);
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());

        log.debug("Found {} groups matching filters", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get groups by subject", description = "Retrieves all groups for a specific subject")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<GroupResponse>> getGroupsBySubject(
            @Parameter(description = "Subject ID") @PathVariable Long subjectId) {
        log.debug("Fetching groups for subject ID: {}", subjectId);

        List<Group> groups = groupService.getGroupsBySubject(subjectId);
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get groups by teacher", description = "Retrieves all groups for a specific teacher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<GroupResponse>> getGroupsByTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId) {
        log.debug("Fetching groups for teacher ID: {}", teacherId);

        List<Group> groups = groupService.getGroupsByTeacher(teacherId);
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    @Operation(summary = "Get groups with available places", description = "Retrieves all groups that have available places")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<GroupResponse>> getGroupsWithAvailablePlaces() {
        log.debug("Fetching groups with available places");

        List<Group> groups = groupService.getGroupsWithAvailablePlaces();
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a group", description = "Deletes a group. Only allowed if no students are enrolled. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete group with enrolled students"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Void> deleteGroup(@Parameter(description = "Group ID") @PathVariable Long id) {
        log.info("Deleting group with ID: {}", id);

        groupService.deleteGroup(id);

        log.info("Group deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Applies dynamic filters using Specifications.
     */
    private List<Group> applyFilters(Long subjectId, Long teacherId, GroupType type, AcademicPeriod period,
                                      GroupStatus status, Classroom classroom, Boolean hasAvailablePlaces, Integer year) {

        // If no filters provided, return all groups
        if (subjectId == null && teacherId == null && type == null && period == null &&
                status == null && classroom == null && hasAvailablePlaces == null && year == null) {
            return groupService.getAllGroups();
        }

        // Build specification with provided filters
        Specification<Group> spec = GroupSpecifications.combineFilters(
                subjectId, teacherId, type, period, status, classroom, hasAvailablePlaces, year
        );

        return groupService.findGroupsWithFilters(spec);
    }
}
