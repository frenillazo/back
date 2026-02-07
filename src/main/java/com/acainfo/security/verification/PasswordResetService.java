package com.acainfo.security.verification;

import com.acainfo.shared.infrastructure.config.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing password reset tokens.
 * Handles creation, validation, and cleanup of reset tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final EmailProperties emailProperties;

    /**
     * Create a new password reset token for user.
     *
     * @param userId User ID
     * @return Generated reset token string
     */
    @Transactional
    public String createPasswordResetToken(Long userId) {
        // Invalidate any existing tokens for this user
        tokenRepository.markAllAsUsedByUserId(userId);

        String tokenString = UUID.randomUUID().toString();
        int expirationHours = emailProperties.getPasswordReset().getExpirationHours();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        PasswordResetToken token = PasswordResetToken.builder()
                .userId(userId)
                .token(tokenString)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .used(false)
                .build();

        tokenRepository.save(token);
        log.info("Created password reset token for user ID: {}", userId);

        return tokenString;
    }

    /**
     * Build the full password reset link URL.
     *
     * @param token Reset token string
     * @return Full reset URL
     */
    public String buildResetLink(String token) {
        String baseUrl = emailProperties.getPasswordReset().getBaseUrl();
        return baseUrl + "?token=" + token;
    }

    /**
     * Validate password reset token.
     *
     * @param token Reset token string
     * @return PasswordResetToken entity if valid
     * @throws InvalidPasswordResetTokenException if token is invalid, expired, or used
     */
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository
                .findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new InvalidPasswordResetTokenException(
                        "Token de recuperacion invalido o expirado"));

        if (!resetToken.isValid()) {
            throw new InvalidPasswordResetTokenException("Token de recuperacion ya utilizado");
        }

        return resetToken;
    }

    /**
     * Mark token as used after successful password reset.
     *
     * @param token Reset token string
     */
    @Transactional
    public void markAsUsed(String token) {
        tokenRepository.findByToken(token).ifPresent(resetToken -> {
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            log.info("Marked password reset token as used for user ID: {}", resetToken.getUserId());
        });
    }

    /**
     * Delete all tokens for a user.
     *
     * @param userId User ID
     */
    @Transactional
    public void deleteUserTokens(Long userId) {
        tokenRepository.deleteByUserId(userId);
        log.info("Deleted all password reset tokens for user ID: {}", userId);
    }

    /**
     * Cleanup expired tokens (can be called from scheduled task).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired password reset tokens");
    }
}
