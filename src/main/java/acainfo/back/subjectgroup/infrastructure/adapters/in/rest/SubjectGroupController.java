package acainfo.back.subjectgroup.infrastructure.adapters.in.rest;

import acainfo.back.shared.domain.model.User;
import acainfo.back.subjectgroup.application.services.SubjectGroupService;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.domain.validation.SubjectGroupSpecifications;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for subjectGroup management.
 * Provides endpoints for CRUD operations and advanced filtering.
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Groups", description = "SubjectGroup management endpoints")
public class SubjectGroupController {

    private final SubjectGroupService subjectGroupService;

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

        // Build subjectGroup entity from request
        SubjectGroup subjectGroup = SubjectGroup.builder()
                .subject(Subject.builder().id(request.getSubjectId()).build())
                .type(request.getType())
                .period(request.getPeriod())
                .maxCapacity(request.getMaxCapacity())
                .build();

        // Set teacher if provided
        if (request.getTeacherId() != null) {
            subjectGroup.setTeacher(User.builder().id(request.getTeacherId()).build());
        }

        SubjectGroup createdSubjectGroup = subjectGroupService.createGroup(subjectGroup);
        SubjectGroupResponse response = SubjectGroupResponse.fromEntity(createdSubjectGroup);

        log.info("SubjectGroup created successfully with ID: {}", createdSubjectGroup.getId());
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
        SubjectGroup subjectGroup = SubjectGroup.builder()
                .type(request.getType())
                .period(request.getPeriod())
                .maxCapacity(request.getMaxCapacity())
                .status(request.getStatus())
                .build();

        // Set teacher if provided
        if (request.getTeacherId() != null) {
            subjectGroup.setTeacher(User.builder().id(request.getTeacherId()).build());
        }

        SubjectGroup updatedSubjectGroup = subjectGroupService.updateGroup(id, subjectGroup);
        SubjectGroupResponse response = SubjectGroupResponse.fromEntity(updatedSubjectGroup);

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

        SubjectGroup updatedSubjectGroup = subjectGroupService.assignTeacher(id, teacherId);
        SubjectGroupResponse response = SubjectGroupResponse.fromEntity(updatedSubjectGroup);

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

        subjectGroupService.cancelGroup(id);

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

        SubjectGroup subjectGroup = subjectGroupService.getGroupById(id);
        SubjectGroupResponse response = SubjectGroupResponse.fromEntity(subjectGroup);

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
            @Parameter(description = "Filter groups with available places") @RequestParam(required = false) Boolean hasAvailablePlaces,
            @Parameter(description = "Filter by year") @RequestParam(required = false) Integer year) {

        log.debug("Fetching subjectGroups with filters - subjectId: {}, teacherId: {}, type: {}, period: {}, " +
                        "status: {}, hasAvailable: {}, year: {}",
                subjectId, teacherId, type, period, status, hasAvailablePlaces, year);

        List<SubjectGroup> subjectGroups = applyFilters(subjectId, teacherId, type, period, status, hasAvailablePlaces, year);
        List<SubjectGroupResponse> responses = subjectGroups.stream()
                .map(SubjectGroupResponse::fromEntity)
                .collect(Collectors.toList());

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

        List<SubjectGroup> subjectGroups = subjectGroupService.getGroupsBySubject(subjectId);
        List<SubjectGroupResponse> responses = subjectGroups.stream()
                .map(SubjectGroupResponse::fromEntity)
                .collect(Collectors.toList());

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

        List<SubjectGroup> subjectGroups = subjectGroupService.getGroupsByTeacher(teacherId);
        List<SubjectGroupResponse> responses = subjectGroups.stream()
                .map(SubjectGroupResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    @Operation(summary = "Get groups with available places", description = "Retrieves all groups that have available places")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    public ResponseEntity<List<SubjectGroupResponse>> getGroupsWithAvailablePlaces() {
        log.debug("Fetching subjectGroups with available places");

        List<SubjectGroup> subjectGroups = subjectGroupService.getGroupsWithAvailablePlaces();
        List<SubjectGroupResponse> responses = subjectGroups.stream()
                .map(SubjectGroupResponse::fromEntity)
                .collect(Collectors.toList());

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

        subjectGroupService.deleteGroup(id);

        log.info("SubjectGroup deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Applies dynamic filters using Specifications.
     */
    private List<SubjectGroup> applyFilters(Long subjectId, Long teacherId, GroupType type, AcademicPeriod period,
                                            GroupStatus status, Boolean hasAvailablePlaces, Integer year) {

        // If no filters provided, return all groups
        if (subjectId == null && teacherId == null && type == null && period == null &&
                status == null && hasAvailablePlaces == null && year == null) {
            return subjectGroupService.getAllGroups();
        }

        // Build specification with provided filters
        Specification<SubjectGroup> spec = SubjectGroupSpecifications.combineFilters(
                subjectId, teacherId, type, period, status, null, hasAvailablePlaces, year
        );

        return subjectGroupService.findGroupsWithFilters(spec);
    }
}
