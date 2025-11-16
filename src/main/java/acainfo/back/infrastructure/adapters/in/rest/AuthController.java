package acainfo.back.infrastructure.adapters.in.rest;

import acainfo.back.application.services.AuthService;
import acainfo.back.infrastructure.adapters.in.dto.AuthResponse;
import acainfo.back.infrastructure.adapters.in.dto.LoginRequest;
import acainfo.back.infrastructure.adapters.in.dto.MessageResponse;
import acainfo.back.infrastructure.adapters.in.dto.RefreshTokenRequest;
import acainfo.back.infrastructure.adapters.in.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - Email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/auth/refresh");
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        log.info("POST /api/auth/logout");
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(refreshToken);
        return ResponseEntity.ok(MessageResponse.success("Logged out successfully"));
    }

    /**
     * Revoke all tokens for current user
     * POST /api/auth/revoke-all
     */
    @PostMapping("/revoke-all")
    public ResponseEntity<MessageResponse> revokeAll() {
        log.info("POST /api/auth/revoke-all");
        authService.revokeAllTokens();
        return ResponseEntity.ok(MessageResponse.success("All tokens revoked successfully"));
    }
}
