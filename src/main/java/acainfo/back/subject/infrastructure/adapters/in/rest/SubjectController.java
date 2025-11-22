package acainfo.back.subject.infrastructure.adapters.in.rest;

import acainfo.back.shared.infrastructure.adapters.in.dto.ErrorResponse;
import acainfo.back.subject.application.mappers.SubjectDtoMapper;
import acainfo.back.subject.application.ports.in.CreateSubjectUseCase;
import acainfo.back.subject.application.ports.in.DeleteSubjectUseCase;
import acainfo.back.subject.application.ports.in.GetSubjectUseCase;
import acainfo.back.subject.application.ports.in.UpdateSubjectUseCase;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.in.dto.CreateSubjectRequest;
import acainfo.back.subject.infrastructure.adapters.in.dto.SubjectResponse;
import acainfo.back.subject.infrastructure.adapters.in.dto.UpdateSubjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * REST Controller for subject management.
 * Uses hexagonal architecture with Use Cases and DTO Mapper.
 * Provides CRUD operations and filtering capabilities.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subjects", description = "Subject management APIs")
public class SubjectController {

    // Use Cases (Ports IN)
    private final CreateSubjectUseCase createSubjectUseCase;
    private final UpdateSubjectUseCase updateSubjectUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final DeleteSubjectUseCase deleteSubjectUseCase;

    // Mapper
    private final SubjectDtoMapper subjectDtoMapper;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new subject",
            description = "Creates a new subject in the system. Only administrators can perform this action."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Subject created successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Subject code already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        log.info("Creating subject with code: {}", request.getCode());

        // Map DTO → Domain
        SubjectDomain subject = subjectDtoMapper.toDomain(request);

        // Execute use case
        SubjectDomain createdSubject = createSubjectUseCase.createSubject(subject);

        // Map Domain → DTO Response
        SubjectResponse response = subjectDtoMapper.toResponse(createdSubject);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== READ ====================

    @GetMapping
    @Operation(
            summary = "Get all subjects",
            description = "Retrieves all subjects with optional filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Subjects retrieved successfully"
    )
    public ResponseEntity<List<SubjectResponse>> getAllSubjects(
            @Parameter(description = "Filter by degree") @RequestParam(required = false) Degree degree,
            @Parameter(description = "Filter by year (1-4)") @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by semester (1-2)") @RequestParam(required = false) Integer semester,
            @Parameter(description = "Filter by status") @RequestParam(required = false) SubjectStatus status,
            @Parameter(description = "Search by code or name") @RequestParam(required = false) String search
    ) {
        log.debug("Getting all subjects with filters - degree: {}, year: {}, semester: {}, status: {}, search: {}",
                degree, year, semester, status, search);

        // Apply filters using use case methods
        List<SubjectDomain> subjects = applyFilters(degree, year, semester, status, search);

        // Map to response
        List<SubjectResponse> response = subjectDtoMapper.toResponses(subjects);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get subject by ID",
            description = "Retrieves a specific subject by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject found",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> getSubjectById(
            @Parameter(description = "Subject ID") @PathVariable Long id
    ) {
        log.debug("Getting subject by ID: {}", id);

        SubjectDomain subject = getSubjectUseCase.getSubjectById(id);
        SubjectResponse response = subjectDtoMapper.toResponse(subject);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(
            summary = "Get subject by code",
            description = "Retrieves a specific subject by its unique code"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject found",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> getSubjectByCode(
            @Parameter(description = "Subject code (e.g., ING-101)") @PathVariable String code
    ) {
        log.debug("Getting subject by code: {}", code);

        SubjectDomain subject = getSubjectUseCase.getSubjectByCode(code);
        SubjectResponse response = subjectDtoMapper.toResponse(subject);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get all active subjects",
            description = "Retrieves all subjects with ACTIVO status"
    )
    @ApiResponse(responseCode = "200", description = "Active subjects retrieved successfully")
    public ResponseEntity<List<SubjectResponse>> getActiveSubjects() {
        log.debug("Getting all active subjects");

        List<SubjectDomain> subjects = getSubjectUseCase.getActiveSubjects();
        List<SubjectResponse> response = subjectDtoMapper.toResponses(subjects);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/degree/{degree}")
    @Operation(
            summary = "Get subjects by degree",
            description = "Retrieves all subjects for a specific engineering degree"
    )
    @ApiResponse(responseCode = "200", description = "Subjects retrieved successfully")
    public ResponseEntity<List<SubjectResponse>> getSubjectsByDegree(
            @Parameter(description = "Engineering degree") @PathVariable Degree degree
    ) {
        log.debug("Getting subjects by degree: {}", degree);

        List<SubjectDomain> subjects = getSubjectUseCase.getSubjectsByDegree(degree);
        List<SubjectResponse> response = subjectDtoMapper.toResponses(subjects);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search subjects",
            description = "Searches subjects by code or name (case insensitive)"
    )
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<List<SubjectResponse>> searchSubjects(
            @Parameter(description = "Search term") @RequestParam String term
    ) {
        log.debug("Searching subjects with term: {}", term);

        List<SubjectDomain> subjects = getSubjectUseCase.searchSubjects(term);
        List<SubjectResponse> response = subjectDtoMapper.toResponses(subjects);

        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a subject",
            description = "Updates an existing subject. Only administrators can perform this action."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject updated successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Subject code already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> updateSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        log.info("Updating subject with ID: {}", id);

        // Load existing subject
        SubjectDomain existing = getSubjectUseCase.getSubjectById(id);

        // Apply updates from DTO
        SubjectDomain updated = subjectDtoMapper.updateDomainFromDto(existing, request);

        // Execute use case
        SubjectDomain updatedSubject = updateSubjectUseCase.updateSubject(id, updated);

        // Map to response
        SubjectResponse response = subjectDtoMapper.toResponse(updatedSubject);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update subject status",
            description = "Changes the status of a subject (ACTIVO, INACTIVO, ARCHIVADO)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public ResponseEntity<SubjectResponse> updateSubjectStatus(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam SubjectStatus status
    ) {
        log.info("Updating status for subject ID: {} to {}", id, status);

        // Load existing subject
        SubjectDomain existing = getSubjectUseCase.getSubjectById(id);

        // Build updated subject with new status
        SubjectDomain updated = SubjectDomain.builder()
                .id(existing.getId())
                .code(existing.getCode())
                .name(existing.getName())
                .year(existing.getYear())
                .degree(existing.getDegree())
                .semester(existing.getSemester())
                .description(existing.getDescription())
                .status(status) // Updated status
                .createdAt(existing.getCreatedAt())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        // Execute use case
        SubjectDomain updatedSubject = updateSubjectUseCase.updateSubject(id, updated);

        // Map to response
        SubjectResponse response = subjectDtoMapper.toResponse(updatedSubject);

        return ResponseEntity.ok(response);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a subject",
            description = "Permanently deletes a subject. Cannot delete subjects with active groups."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Subject deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Subject has active groups",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id
    ) {
        log.info("Deleting subject with ID: {}", id);

        deleteSubjectUseCase.deleteSubject(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Archive a subject",
            description = "Soft delete - changes subject status to ARCHIVADO"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject archived successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> archiveSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id
    ) {
        log.info("Archiving subject with ID: {}", id);

        // Archive using use case (uses domain logic)
        deleteSubjectUseCase.archiveSubject(id);

        // Get updated subject
        SubjectDomain subject = getSubjectUseCase.getSubjectById(id);
        SubjectResponse response = subjectDtoMapper.toResponse(subject);

        return ResponseEntity.ok(response);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Applies filters to subject query using Use Case methods.
     * Supports dynamic combination of multiple filters.
     * If no filters are provided, returns all subjects.
     */
    private List<SubjectDomain> applyFilters(Degree degree, Integer year, Integer semester,
                                              SubjectStatus status, String search) {
        // Priority 1: Search by term (most specific)
        if (search != null && !search.isBlank()) {
            return getSubjectUseCase.searchSubjects(search);
        }

        // Priority 2: Combined filters (degree + year)
        if (degree != null && year != null) {
            return getSubjectUseCase.getSubjectsByDegreeAndYear(degree, year);
        }

        // Priority 3: Single filters
        if (status != null) {
            return getSubjectUseCase.getSubjectsByStatus(status);
        }

        if (degree != null) {
            return getSubjectUseCase.getSubjectsByDegree(degree);
        }

        // No filters: return all
        return getSubjectUseCase.getAllSubjects();
    }
}
