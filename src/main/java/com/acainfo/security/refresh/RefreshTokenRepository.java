package com.acainfo.security.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA repository for RefreshToken.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find valid (non-revoked, non-expired) refresh token by token string.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token " +
            "AND rt.revoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidToken(String token, LocalDateTime now);

    /**
     * Delete all tokens for a user.
     */
    void deleteByUserId(Long userId);

    /**
     * Revoke all tokens for a user.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllByUserId(Long userId);

    /**
     * Delete expired tokens (cleanup task).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
