package com.acainfo.security.refresh;

import com.acainfo.security.jwt.JwtProperties;
import com.acainfo.shared.domain.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing refresh tokens.
 * Handles creation, validation, revocation, and cleanup of refresh tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * Create a new refresh token for user.
     *
     * @param userId User ID
     * @return Generated refresh token string
     */
    @Transactional
    public String createRefreshToken(Long userId) {
        String tokenString = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(tokenString)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user ID: {}", userId);

        return tokenString;
    }

    /**
     * Validate refresh token.
     *
     * @param token Refresh token string
     * @return RefreshToken entity if valid
     * @throws InvalidRefreshTokenException if token is invalid, expired, or revoked
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid or expired refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException("Refresh token is not valid");
        }

        return refreshToken;
    }

    /**
     * Revoke a specific refresh token.
     *
     * @param token Refresh token string
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Revoked refresh token for user ID: {}", refreshToken.getUserId());
        });
    }

    /**
     * Revoke all refresh tokens for a user (e.g., on logout from all devices).
     *
     * @param userId User ID
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked all refresh tokens for user ID: {}", userId);
    }

    /**
     * Delete all refresh tokens for a user.
     *
     * @param userId User ID
     */
    @Transactional
    public void deleteUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Deleted all refresh tokens for user ID: {}", userId);
    }

    /**
     * Cleanup expired tokens (scheduled task).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    /**
     * Exception thrown when refresh token is invalid.
     */
    public static class InvalidRefreshTokenException extends BusinessRuleException {
        public InvalidRefreshTokenException(String message) {
            super(message);
        }

        @Override
        public String getErrorCode() {
            return "INVALID_REFRESH_TOKEN";
        }
    }
}
