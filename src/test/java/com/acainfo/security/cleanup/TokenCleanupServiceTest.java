package com.acainfo.security.cleanup;

import com.acainfo.security.refresh.RefreshTokenService;
import com.acainfo.security.verification.EmailVerificationService;
import com.acainfo.security.verification.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Pure unit tests for {@link TokenCleanupService}.
 */
@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;

    @Test
    void cleanupExpiredTokens_invokesAllThreeServices() {
        tokenCleanupService.cleanupExpiredTokens();

        verify(refreshTokenService).cleanupExpiredTokens();
        verify(emailVerificationService).cleanupExpiredTokens();
        verify(passwordResetService).cleanupExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_continuesWhenOneServiceFails() {
        doThrow(new RuntimeException("db error"))
                .when(refreshTokenService).cleanupExpiredTokens();

        tokenCleanupService.cleanupExpiredTokens();

        verify(emailVerificationService).cleanupExpiredTokens();
        verify(passwordResetService).cleanupExpiredTokens();
    }
}
