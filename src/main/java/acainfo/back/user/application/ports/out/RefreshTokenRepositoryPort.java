package acainfo.back.user.application.ports.out;

import acainfo.back.user.domain.model.RefreshTokenDomain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for RefreshToken repository operations.
 * Defines the contract for refresh token persistence.
 * Works with RefreshTokenDomain (pure domain model).
 */
public interface RefreshTokenRepositoryPort {

    /**
     * Saves a refresh token (create or update).
     *
     * @param refreshToken the refresh token to save
     * @return the saved refresh token
     */
    RefreshTokenDomain save(RefreshTokenDomain refreshToken);

    /**
     * Finds a refresh token by ID.
     *
     * @param id the refresh token ID
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshTokenDomain> findById(Long id);

    /**
     * Finds a refresh token by token string.
     *
     * @param token the token string
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshTokenDomain> findByToken(String token);

    /**
     * Finds all refresh tokens for a user.
     *
     * @param userId the user ID
     * @return list of refresh tokens
     */
    List<RefreshTokenDomain> findByUserId(Long userId);

    /**
     * Finds all valid (non-revoked and non-expired) tokens for a user.
     *
     * @param userId the user ID
     * @param now current datetime
     * @return list of valid refresh tokens
     */
    List<RefreshTokenDomain> findValidTokensByUserId(Long userId, LocalDateTime now);

    /**
     * Revokes all tokens for a user.
     *
     * @param userId the user ID
     */
    void revokeAllUserTokens(Long userId);

    /**
     * Deletes expired tokens (for cleanup).
     *
     * @param now current datetime
     */
    void deleteExpiredTokens(LocalDateTime now);

    /**
     * Deletes all tokens for a specific user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Deletes a refresh token by ID.
     *
     * @param id the refresh token ID
     */
    void deleteById(Long id);

    /**
     * Checks if a refresh token exists by ID.
     *
     * @param id the refresh token ID
     * @return true if refresh token exists, false otherwise
     */
    boolean existsById(Long id);
}
