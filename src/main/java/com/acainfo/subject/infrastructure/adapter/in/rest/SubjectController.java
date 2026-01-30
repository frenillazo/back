package com.acainfo.subject.infrastructure.adapter.in.rest;

import com.acainfo.subject.application.dto.CreateSubjectCommand;
import com.acainfo.subject.application.dto.SubjectFilters;
import com.acainfo.subject.application.dto.UpdateSubjectCommand;
import com.acainfo.subject.application.port.in.CreateSubjectUseCase;
import com.acainfo.subject.application.port.in.DeleteSubjectUseCase;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.application.port.in.UpdateSubjectUseCase;
import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.domain.model.SubjectStatus;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.CreateSubjectRequest;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.SubjectResponse;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.UpdateSubjectRequest;
import com.acainfo.subject.infrastructure.mapper.SubjectRestMapper;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.MessageResponse;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for subject management.
 * Handles CRUD operations for subjects.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subject Management", description = "Subject CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class SubjectController {

    private final CreateSubjectUseCase createSubjectUseCase;
    private final UpdateSubjectUseCase updateSubjectUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final DeleteSubjectUseCase deleteSubjectUseCase;
    private final SubjectRestMapper subjectRestMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create subject",
            description = "Creates a new subject (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Subject created successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or subject code already exists",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        log.info("Create subject request for code: {}", request.code());

        CreateSubjectCommand command = subjectRestMapper.toCreateSubjectCommand(request);
        Subject subject = createSubjectUseCase.create(command);
        SubjectResponse response = subjectRestMapper.toSubjectResponse(subject);

        log.info("Subject created successfully: {}", subject.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List subjects",
            description = "Returns paginated list of subjects with optional filters"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subjects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<SubjectResponse>> getSubjects(
            @Parameter(description = "Filter by code (exact match)")
            @RequestParam(required = false) String code,

            @Parameter(description = "Search in code, name")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Filter by degree")
            @RequestParam(required = false) Degree degree,

            @Parameter(description = "Filter by academic year (1-4)")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, ARCHIVED)")
            @RequestParam(required = false) SubjectStatus status,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction (ASC, DESC)")
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("Get subjects request with filters");

        SubjectFilters filters = new SubjectFilters(
                code,
                searchTerm,
                degree,
                year,
                status,
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<Subject> subjectsPage = getSubjectUseCase.findWithFilters(filters);
        Page<SubjectResponse> responsePage = subjectsPage.map(subjectRestMapper::toSubjectResponse);
        PageResponse<SubjectResponse> response = PageResponse.of(responsePage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get subject by ID",
            description = "Returns subject details by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> getSubjectById(
            @Parameter(description = "Subject ID") @PathVariable Long id) {
        log.info("Get subject by ID request: {}", id);

        Subject subject = getSubjectUseCase.getById(id);
        SubjectResponse response = subjectRestMapper.toSubjectResponse(subject);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(
            summary = "Get subject by code",
            description = "Returns subject details by code"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> getSubjectByCode(
            @Parameter(description = "Subject code") @PathVariable String code) {
        log.info("Get subject by code request: {}", code);

        Subject subject = getSubjectUseCase.getByCode(code);
        SubjectResponse response = subjectRestMapper.toSubjectResponse(subject);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update subject",
            description = "Updates subject's name and status (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject updated successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> updateSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        log.info("Update subject request for ID: {}", id);

        UpdateSubjectCommand command = subjectRestMapper.toUpdateSubjectCommand(request);
        Subject subject = updateSubjectUseCase.update(id, command);
        SubjectResponse response = subjectRestMapper.toSubjectResponse(subject);

        log.info("Subject updated successfully: {}", subject.getCode());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete subject",
            description = "Hard deletes subject (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject deleted successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> deleteSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id) {
        log.info("Delete subject request for ID: {}", id);

        deleteSubjectUseCase.delete(id);

        log.info("Subject deleted successfully: ID {}", id);
        return ResponseEntity.ok(MessageResponse.of("Subject deleted successfully"));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Archive subject",
            description = "Soft deletes subject (sets status to ARCHIVED) (ADMIN only)"
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
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<SubjectResponse> archiveSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id) {
        log.info("Archive subject request for ID: {}", id);

        Subject subject = deleteSubjectUseCase.archive(id);
        SubjectResponse response = subjectRestMapper.toSubjectResponse(subject);

        log.info("Subject archived successfully: {}", subject.getCode());
        return ResponseEntity.ok(response);
    }
}
