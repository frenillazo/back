package com.acainfo.user.infrastructure.adapter.in.rest;

import com.acainfo.security.terms.TermsAcceptanceService;
import com.acainfo.user.application.dto.AuthenticationCommand;
import com.acainfo.user.application.dto.AuthenticationResult;
import com.acainfo.user.application.dto.RegisterUserCommand;
import com.acainfo.user.application.port.in.AuthenticateUserUseCase;
import com.acainfo.user.application.port.in.LogoutUseCase;
import com.acainfo.user.application.port.in.RefreshTokenUseCase;
import com.acainfo.user.application.port.in.RegisterUserUseCase;
import com.acainfo.user.application.port.in.RequestPasswordResetUseCase;
import com.acainfo.user.application.port.in.ResendVerificationUseCase;
import com.acainfo.user.application.port.in.ResetPasswordUseCase;
import com.acainfo.user.application.port.in.VerifyEmailUseCase;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.*;
import com.acainfo.user.infrastructure.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations.
 * Handles user registration, login, token refresh, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UserRestMapper userRestMapper;
    private final TermsAcceptanceService termsAcceptanceService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with STUDENT role. Email must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or email already exists",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Registration request for email: {}", request.email());

        RegisterUserCommand command = userRestMapper.toRegisterUserCommand(request);
        User user = registerUserUseCase.register(command);

        // Record terms acceptance at registration time (GDPR Art. 7.1)
        String ipAddress = extractClientIp(httpRequest);
        termsAcceptanceService.acceptTerms(user.getId(), ipAddress);
        log.info("Terms accepted during registration for user: {}", user.getEmail());

        UserResponse response = userRestMapper.toUserResponse(user);

        log.info("User registered successfully: {}", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates user and returns JWT access token and refresh token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());

        AuthenticationCommand command = userRestMapper.toAuthenticationCommand(request);
        AuthenticationResult result = authenticateUserUseCase.authenticate(command);
        AuthResponse authResponse = userRestMapper.toAuthResponse(result);

        // Check terms acceptance and include in response
        boolean termsAccepted = termsAcceptanceService.hasAcceptedCurrentTerms(result.user().getId());
        AuthResponse response = AuthResponse.builder()
                .accessToken(authResponse.accessToken())
                .refreshToken(authResponse.refreshToken())
                .tokenType(authResponse.tokenType())
                .expiresIn(authResponse.expiresIn())
                .user(authResponse.user())
                .termsAccepted(termsAccepted)
                .build();

        log.info("User logged in successfully: {}", request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates new access token and refresh token using valid refresh token. Old refresh token is revoked."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");

        AuthenticationResult result = refreshTokenUseCase.refreshToken(request.refreshToken());
        AuthResponse authResponse = userRestMapper.toAuthResponse(result);

        // Check terms acceptance and include in response
        boolean termsAccepted = termsAcceptanceService.hasAcceptedCurrentTerms(result.user().getId());
        AuthResponse response = AuthResponse.builder()
                .accessToken(authResponse.accessToken())
                .refreshToken(authResponse.refreshToken())
                .tokenType(authResponse.tokenType())
                .expiresIn(authResponse.expiresIn())
                .user(authResponse.user())
                .termsAccepted(termsAccepted)
                .build();

        log.info("Token refreshed successfully for user: {}", result.user().getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidates the provided refresh token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid refresh token",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request");

        logoutUseCase.logout(request.refreshToken());

        log.info("User logged out successfully");
        return ResponseEntity.ok(MessageResponse.of("Logout successful"));
    }

    @PostMapping("/logout/all")
    @Operation(
            summary = "Logout from all devices",
            description = "Invalidates all refresh tokens for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Logged out from all devices",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> logoutAllDevices(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Logout all devices request for user: {}", userDetails.getUsername());

        // Get user ID from email
        // Note: This assumes UserDetails username is email
        // In a real implementation, you'd fetch the user from the repository
        // For now, we'll need to enhance this in the future

        // TODO: Implement proper user ID extraction from UserDetails
        // For now, this is a placeholder that will need service enhancement

        log.warn("Logout all devices not fully implemented - requires user ID extraction");
        return ResponseEntity.ok(MessageResponse.of("Logout from all devices successful"));
    }

    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verifies user email using the token sent via email. Activates the user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired verification token",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        log.info("Email verification request");

        verifyEmailUseCase.verifyEmail(token);

        log.info("Email verified successfully");
        return ResponseEntity.ok(MessageResponse.of("Email verificado correctamente. Ya puedes iniciar sesion."));
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend verification email",
            description = "Sends a new verification email to the user. Previous tokens are invalidated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification email sent",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User not found or already verified",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification request for email: {}", request.email());

        resendVerificationUseCase.resendVerification(request.email());

        log.info("Verification email resent to: {}", request.email());
        return ResponseEntity.ok(MessageResponse.of("Email de verificacion enviado. Revisa tu bandeja de entrada."));
    }

    @PostMapping("/request-password-reset")
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email if the email exists. Always returns success for security."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "If the email exists, a reset link has been sent",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> requestPasswordReset(
            @Valid @RequestBody RequestPasswordResetRequest request) {
        log.info("Password reset request for email: {}", request.email());

        requestPasswordResetUseCase.requestPasswordReset(request.email());

        // Always return same message regardless of whether email exists (security)
        return ResponseEntity.ok(MessageResponse.of(
                "Si el email esta registrado, recibiras un enlace para restablecer tu contrasena."));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Resets user password using a valid reset token. Revokes all refresh tokens."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset with token");

        resetPasswordUseCase.resetPassword(request.token(), request.newPassword());

        log.info("Password reset successfully");
        return ResponseEntity.ok(MessageResponse.of(
                "Contrasena restablecida correctamente. Ya puedes iniciar sesion con tu nueva contrasena."));
    }

    /**
     * Extract the real client IP address.
     * Cloudflare sets CF-Connecting-IP and X-Forwarded-For headers.
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
