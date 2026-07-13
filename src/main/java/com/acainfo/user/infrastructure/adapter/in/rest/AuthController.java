package com.acainfo.user.infrastructure.adapter.in.rest;

import com.acainfo.security.refresh.AuthCookieService;
import com.acainfo.security.refresh.RefreshTokenService;
import com.acainfo.security.terms.TermsAcceptanceService;
import com.acainfo.security.userdetails.CustomUserDetails;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AuthCookieService authCookieService;

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
                .tokenType(authResponse.tokenType())
                .expiresIn(authResponse.expiresIn())
                .user(authResponse.user())
                .termsAccepted(termsAccepted)
                .build();

        // El refresh token viaja SOLO en cookie httpOnly, nunca en el body.
        ResponseCookie refreshCookie = authCookieService.buildRefreshCookie(result.refreshToken());

        log.info("User logged in successfully: {}", request.email());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
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
                    description = "Refresh token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        log.info("Token refresh request");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenService.InvalidRefreshTokenException("No hay refresh token");
        }

        AuthenticationResult result = refreshTokenUseCase.refreshToken(refreshToken);
        AuthResponse authResponse = userRestMapper.toAuthResponse(result);

        // Check terms acceptance and include in response
        boolean termsAccepted = termsAcceptanceService.hasAcceptedCurrentTerms(result.user().getId());
        AuthResponse response = AuthResponse.builder()
                .accessToken(authResponse.accessToken())
                .tokenType(authResponse.tokenType())
                .expiresIn(authResponse.expiresIn())
                .user(authResponse.user())
                .termsAccepted(termsAccepted)
                .build();

        // Rotación: el token nuevo reemplaza la cookie httpOnly.
        ResponseCookie refreshCookie = authCookieService.buildRefreshCookie(result.refreshToken());

        log.info("Token refreshed successfully for user: {}", result.user().getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidates the provided refresh token. Body is optional; without a token the logout is client-side only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        log.info("Logout request");

        if (refreshToken != null && !refreshToken.isBlank()) {
            logoutUseCase.logout(refreshToken);
            log.info("User logged out and refresh token revoked");
        } else {
            log.warn("Logout request without refresh token cookie - nothing to revoke");
        }

        // Borra la cookie del navegador aunque no hubiera token que revocar.
        ResponseCookie clearCookie = authCookieService.buildClearRefreshCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(MessageResponse.of("Sesión cerrada correctamente"));
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
    public ResponseEntity<MessageResponse> logoutAllDevices(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.of("Authentication required"));
        }

        log.info("Logout all devices request for user: {}", userDetails.getUsername());

        logoutUseCase.logoutAllDevices(userDetails.getUserId());

        log.info("User logged out from all devices: {}", userDetails.getUsername());
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
