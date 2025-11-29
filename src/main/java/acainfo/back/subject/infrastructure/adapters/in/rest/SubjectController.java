package acainfo.back.subject.infrastructure.adapters.in.rest;

import acainfo.back.config.dto.ErrorResponse;
import acainfo.back.subject.application.ports.in.CreateSubjectUseCase;
import acainfo.back.subject.application.ports.in.DeleteSubjectUseCase;
import acainfo.back.subject.application.ports.in.GetSubjectUseCase;
import acainfo.back.subject.application.ports.in.UpdateSubjectUseCase;
import acainfo.back.subject.application.services.SubjectService;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.domain.validation.SubjectSpecifications;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for subject management.
 * Provides CRUD operations and filtering capabilities.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subjects", description = "Subject management APIs")
public class SubjectController {

    private final CreateSubjectUseCase createSubjectUseCase;
    private final UpdateSubjectUseCase updateSubjectUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final DeleteSubjectUseCase deleteSubjectUseCase;
    private final SubjectService subjectService;

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

        Subject subject = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .year(request.getYear())
                .degree(request.getDegree())
                .semester(request.getSemester())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();

        Subject createdSubject = createSubjectUseCase.createSubject(subject);
        SubjectResponse response = SubjectResponse.fromEntity(createdSubject);

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

        List<Subject> subjects = applyFilters(degree, year, semester, status, search);
        List<SubjectResponse> response = subjects.stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());

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

        Subject subject = getSubjectUseCase.getSubjectById(id);
        SubjectResponse response = SubjectResponse.fromEntity(subject);

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

        Subject subject = getSubjectUseCase.getSubjectByCode(code);
        SubjectResponse response = SubjectResponse.fromEntity(subject);

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

        List<Subject> subjects = getSubjectUseCase.getActiveSubjects();
        List<SubjectResponse> response = subjects.stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());

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

        List<Subject> subjects = getSubjectUseCase.getSubjectsByDegree(degree);
        List<SubjectResponse> response = subjects.stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());

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

        List<Subject> subjects = getSubjectUseCase.searchSubjects(term);
        List<SubjectResponse> response = subjects.stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());

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

        Subject subject = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .year(request.getYear())
                .degree(request.getDegree())
                .semester(request.getSemester())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();

        Subject updatedSubject = updateSubjectUseCase.updateSubject(id, subject);
        SubjectResponse response = SubjectResponse.fromEntity(updatedSubject);

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

        Subject subject = getSubjectUseCase.getSubjectById(id);
        subject.setStatus(status);

        Subject updatedSubject = updateSubjectUseCase.updateSubject(id, subject);
        SubjectResponse response = SubjectResponse.fromEntity(updatedSubject);

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

        deleteSubjectUseCase.archiveSubject(id);
        Subject subject = getSubjectUseCase.getSubjectById(id);
        SubjectResponse response = SubjectResponse.fromEntity(subject);

        return ResponseEntity.ok(response);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Applies filters to subject query using Specifications (Criteria API).
     * Supports dynamic combination of multiple filters.
     * If no filters are provided, returns all subjects.
     */
    private List<Subject> applyFilters(Degree degree, Integer year, Integer semester, SubjectStatus status, String search) {
        // Build dynamic specification combining all filters
        Specification<Subject> spec = SubjectSpecifications.combineFilters(
                degree,
                year,
                semester,
                status,
                search
        );

        // Use SubjectService with Specifications for dynamic filtering
        return subjectService.findSubjectsWithFilters(spec);
    }
}
