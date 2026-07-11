package com.acainfo.security.cleanup;

import com.acainfo.security.refresh.RefreshTokenService;
import com.acainfo.security.verification.EmailVerificationService;
import com.acainfo.security.verification.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled cleanup of expired security tokens.
 * Invokes the cleanup methods of the refresh, email verification and
 * password reset token services, which otherwise are never called.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    /**
     * Scheduled job that runs every day at 5:00 AM (configurable).
     * Each cleanup runs in its own transaction; a failure in one does not skip the others.
     */
    @Scheduled(cron = "${app.cleanup.expired-tokens.cron:0 0 5 * * *}")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled job: cleanupExpiredTokens");

        runCleanup("refresh tokens", refreshTokenService::cleanupExpiredTokens);
        runCleanup("email verification tokens", emailVerificationService::cleanupExpiredTokens);
        runCleanup("password reset tokens", passwordResetService::cleanupExpiredTokens);

        log.info("Scheduled job cleanupExpiredTokens completed");
    }

    private void runCleanup(String tokenType, Runnable cleanup) {
        try {
            cleanup.run();
        } catch (Exception e) {
            log.error("Error cleaning up expired {}: {}", tokenType, e.getMessage());
        }
    }
}
