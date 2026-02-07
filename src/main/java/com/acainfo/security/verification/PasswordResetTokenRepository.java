package com.acainfo.security.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA repository for PasswordResetToken.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find reset token by token string.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find valid (non-used, non-expired) reset token by token string.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token " +
            "AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(String token, LocalDateTime now);

    /**
     * Delete all tokens for a user.
     */
    void deleteByUserId(Long userId);

    /**
     * Mark all tokens for a user as used.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.userId = :userId")
    void markAllAsUsedByUserId(Long userId);

    /**
     * Delete expired tokens (cleanup task).
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
