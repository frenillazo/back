package com.acainfo.user.infrastructure.adapter.in.rest;

import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.user.application.dto.UpdateUserCommand;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.application.port.in.UpdateUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.ChangePasswordRequest;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.MessageResponse;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.UpdateProfileRequest;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.UserResponse;
import com.acainfo.user.infrastructure.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user profile operations.
 * Handles authenticated user's profile management.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UserRestMapper userRestMapper;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get user profile",
            description = "Returns the profile of the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get profile request for user: {}", userDetails.getUsername());

        Long userId = userDetails.getUser().getId();
        User user = getUserProfileUseCase.getUserById(userId);
        UserResponse response = userRestMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update user profile",
            description = "Updates first name and last name of the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile request for user: {}", userDetails.getUsername());

        Long userId = userDetails.getUser().getId();
        UpdateUserCommand command = userRestMapper.toUpdateUserCommand(request);
        User user = updateUserProfileUseCase.updateProfile(userId, command);
        UserResponse response = userRestMapper.toUserResponse(user);

        log.info("Profile updated successfully for user: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Change password",
            description = "Changes the password of the authenticated user. Requires current password."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid current password or new password too short",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for user: {}", userDetails.getUsername());

        Long userId = userDetails.getUser().getId();
        updateUserProfileUseCase.changePassword(
                userId,
                request.currentPassword(),
                request.newPassword()
        );

        log.info("Password changed successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.of("Password changed successfully"));
    }
}
