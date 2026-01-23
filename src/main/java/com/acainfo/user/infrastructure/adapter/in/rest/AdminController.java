package com.acainfo.user.infrastructure.adapter.in.rest;

import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.application.port.in.ManageUserRolesUseCase;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.AssignRoleRequest;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.MessageResponse;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.PageResponse;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.RevokeRoleRequest;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.UpdateUserStatusRequest;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.UserResponse;
import com.acainfo.user.infrastructure.mapper.UserRestMapper;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.acainfo.user.application.port.out.UserRepositoryPort;

/**
 * REST Controller for admin operations.
 * Handles user management and system administration tasks.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "System administration endpoints (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final ManageUserRolesUseCase manageUserRolesUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final UserRestMapper userRestMapper;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all users",
            description = "Returns paginated list of all users with optional filters (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Filter by email (exact match)")
            @RequestParam(required = false) String email,

            @Parameter(description = "Search in email, firstName, lastName")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Filter by status (ACTIVE, BLOCKED, etc.)")
            @RequestParam(required = false) UserStatus status,

            @Parameter(description = "Filter by role (ADMIN, TEACHER, STUDENT)")
            @RequestParam(required = false) RoleType roleType,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Get all users request with filters");

        UserFilters filters = new UserFilters(
                email,
                searchTerm,
                status,
                roleType,
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<User> usersPage = userRepositoryPort.findWithFilters(filters);
        Page<UserResponse> responsePage = usersPage.map(userRestMapper::toUserResponse);
        PageResponse<UserResponse> response = PageResponse.of(responsePage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by ID",
            description = "Returns user details by ID (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Get user by ID request: {}", id);

        User user = getUserProfileUseCase.getUserById(id);
        UserResponse response = userRestMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by email",
            description = "Returns user details by email (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "User email") @PathVariable String email) {
        log.info("Get user by email request: {}", email);

        User user = getUserProfileUseCase.getUserByEmail(email);
        UserResponse response = userRestMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/roles/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Assign role to user",
            description = "Assigns a role (ADMIN, TEACHER) to a user (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Role assigned successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User already has the role",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> assignRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request) {
        log.info("Assign role {} to user {}", request.roleType(), id);

        User user = manageUserRolesUseCase.assignRole(id, request.roleType());
        UserResponse response = userRestMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/roles/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Revoke role from user",
            description = "Revokes a role (ADMIN, TEACHER) from a user (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Role revoked successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User doesn't have the role or cannot remove last role",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> revokeRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody RevokeRoleRequest request) {
        log.info("Revoke role {} from user {}", request.roleType(), id);

        User user = manageUserRolesUseCase.revokeRole(id, request.roleType());
        UserResponse response = userRestMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user status",
            description = "Updates user status (ACTIVE, BLOCKED) (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("Update status for user {} to {}", id, request.status());

        User user = manageUserRolesUseCase.updateStatus(id, request.status());
        UserResponse response = userRestMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }
}
