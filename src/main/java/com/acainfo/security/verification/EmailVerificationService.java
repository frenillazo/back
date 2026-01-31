package com.acainfo.security.verification;

import com.acainfo.shared.infrastructure.config.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing email verification tokens.
 * Handles creation, validation, and cleanup of verification tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailProperties emailProperties;

    /**
     * Create a new verification token for user.
     *
     * @param userId User ID
     * @return Generated verification token string
     */
    @Transactional
    public String createVerificationToken(Long userId) {
        // Invalidate any existing tokens for this user
        tokenRepository.markAllAsUsedByUserId(userId);

        String tokenString = UUID.randomUUID().toString();
        int expirationHours = emailProperties.getVerification().getExpirationHours();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(userId)
                .token(tokenString)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .used(false)
                .build();

        tokenRepository.save(token);
        log.info("Created verification token for user ID: {}", userId);

        return tokenString;
    }

    /**
     * Build the full verification link URL.
     *
     * @param token Verification token string
     * @return Full verification URL
     */
    public String buildVerificationLink(String token) {
        String baseUrl = emailProperties.getVerification().getBaseUrl();
        return baseUrl + "?token=" + token;
    }

    /**
     * Validate verification token.
     *
     * @param token Verification token string
     * @return EmailVerificationToken entity if valid
     * @throws InvalidVerificationTokenException if token is invalid, expired, or used
     */
    @Transactional(readOnly = true)
    public EmailVerificationToken validateToken(String token) {
        EmailVerificationToken verificationToken = tokenRepository
                .findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new InvalidVerificationTokenException("Token de verificacion invalido o expirado"));

        if (!verificationToken.isValid()) {
            throw new InvalidVerificationTokenException("Token de verificacion ya utilizado");
        }

        return verificationToken;
    }

    /**
     * Mark token as used after successful verification.
     *
     * @param token Verification token string
     */
    @Transactional
    public void markAsUsed(String token) {
        tokenRepository.findByToken(token).ifPresent(verificationToken -> {
            verificationToken.setUsed(true);
            tokenRepository.save(verificationToken);
            log.info("Marked verification token as used for user ID: {}", verificationToken.getUserId());
        });
    }

    /**
     * Check if user has a pending verification token.
     *
     * @param userId User ID
     * @return true if user has a valid pending token
     */
    @Transactional(readOnly = true)
    public boolean hasPendingVerification(Long userId) {
        return tokenRepository.existsValidTokenByUserId(userId, LocalDateTime.now());
    }

    /**
     * Delete all tokens for a user.
     *
     * @param userId User ID
     */
    @Transactional
    public void deleteUserTokens(Long userId) {
        tokenRepository.deleteByUserId(userId);
        log.info("Deleted all verification tokens for user ID: {}", userId);
    }

    /**
     * Cleanup expired tokens (scheduled task).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired verification tokens");
    }
}
