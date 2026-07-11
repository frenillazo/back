package com.acainfo.user.application.service;

import com.acainfo.security.jwt.JwtTokenProvider;
import com.acainfo.security.refresh.RefreshTokenService;
import com.acainfo.security.verification.EmailVerificationService;
import com.acainfo.security.verification.PasswordResetService;
import com.acainfo.shared.application.port.out.EmailSenderPort;
import com.acainfo.user.application.port.out.RoleRepositoryPort;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.verify;

/**
 * Pure unit tests for the logout use cases of {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceLogoutTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private RoleRepositoryPort roleRepositoryPort;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private PasswordResetService passwordResetService;
    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private AuthService authService;

    @Test
    void logout_revokesTheRefreshToken() {
        authService.logout("refresh-token-123");

        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");
    }

    @Test
    void logoutAllDevices_revokesAllUserTokens() {
        authService.logoutAllDevices(42L);

        verify(refreshTokenService).revokeAllUserTokens(42L);
    }
}
