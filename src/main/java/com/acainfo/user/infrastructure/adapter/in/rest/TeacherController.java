package com.acainfo.user.infrastructure.adapter.in.rest;

import com.acainfo.user.application.dto.CreateTeacherCommand;
import com.acainfo.user.application.dto.UpdateTeacherCommand;
import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.application.port.in.ManageTeachersUseCase;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.*;
import com.acainfo.user.infrastructure.mapper.UserRestMapper;
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
 * REST Controller for teacher management (ADMIN only).
 * Handles CRUD operations for teachers.
 */
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Management", description = "Teacher CRUD operations (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final ManageTeachersUseCase manageTeachersUseCase;
    private final UserRestMapper userRestMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create teacher",
            description = "Creates a new teacher account with TEACHER role (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Teacher created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or email already exists",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        log.info("Create teacher request for email: {}", request.email());

        CreateTeacherCommand command = userRestMapper.toCreateTeacherCommand(request);
        User teacher = manageTeachersUseCase.createTeacher(command);
        TeacherResponse response = userRestMapper.toTeacherResponse(teacher);

        log.info("Teacher created successfully: {}", teacher.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List teachers",
            description = "Returns paginated list of teachers with optional filters (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Teachers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<TeacherResponse>> getTeachers(
            @Parameter(description = "Filter by email (exact match)")
            @RequestParam(required = false) String email,

            @Parameter(description = "Search in email, firstName, lastName")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Filter by status (ACTIVE, BLOCKED, etc.)")
            @RequestParam(required = false) UserStatus status,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Get teachers request with filters");

        UserFilters filters = new UserFilters(
                email,
                searchTerm,
                status,
                null, // roleType forced to TEACHER by service
                null, // degree not applicable to teachers
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<User> teachersPage = manageTeachersUseCase.getTeachers(filters);
        Page<TeacherResponse> responsePage = teachersPage.map(userRestMapper::toTeacherResponse);
        PageResponse<TeacherResponse> response = PageResponse.of(responsePage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get teacher by ID",
            description = "Returns teacher details by ID (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Teacher not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<TeacherResponse> getTeacherById(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {
        log.info("Get teacher by ID request: {}", id);

        User teacher = manageTeachersUseCase.getTeacherById(id);
        TeacherResponse response = userRestMapper.toTeacherResponse(teacher);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update teacher",
            description = "Updates teacher's first name and last name (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Teacher not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<TeacherResponse> updateTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherRequest request) {
        log.info("Update teacher request for ID: {}", id);

        UpdateTeacherCommand command = userRestMapper.toUpdateTeacherCommand(request);
        User teacher = manageTeachersUseCase.updateTeacher(id, command);
        TeacherResponse response = userRestMapper.toTeacherResponse(teacher);

        log.info("Teacher updated successfully: {}", teacher.getEmail());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete teacher",
            description = "Soft deletes teacher (sets status to BLOCKED) (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher deleted successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Teacher not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> deleteTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {
        log.info("Delete teacher request for ID: {}", id);

        manageTeachersUseCase.deleteTeacher(id);

        log.info("Teacher deleted successfully: ID {}", id);
        return ResponseEntity.ok(MessageResponse.of("Teacher deleted successfully"));
    }
}
