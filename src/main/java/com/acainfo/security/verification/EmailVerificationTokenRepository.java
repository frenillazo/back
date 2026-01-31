package com.acainfo.security.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA repository for EmailVerificationToken.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * Find verification token by token string.
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Find valid (non-used, non-expired) verification token by token string.
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.token = :token " +
            "AND t.used = false AND t.expiresAt > :now")
    Optional<EmailVerificationToken> findValidToken(String token, LocalDateTime now);

    /**
     * Find the latest valid token for a user.
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.userId = :userId " +
            "AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC LIMIT 1")
    Optional<EmailVerificationToken> findLatestValidTokenByUserId(Long userId, LocalDateTime now);

    /**
     * Delete all tokens for a user.
     */
    void deleteByUserId(Long userId);

    /**
     * Mark all tokens for a user as used.
     */
    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.used = true WHERE t.userId = :userId")
    void markAllAsUsedByUserId(Long userId);

    /**
     * Delete expired tokens (cleanup task).
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    /**
     * Check if user has a pending (valid) verification token.
     */
    @Query("SELECT COUNT(t) > 0 FROM EmailVerificationToken t WHERE t.userId = :userId " +
            "AND t.used = false AND t.expiresAt > :now")
    boolean existsValidTokenByUserId(Long userId, LocalDateTime now);
}
