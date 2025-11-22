package acainfo.back.user.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

/**
 * RefreshToken domain model.
 * Pure POJO - NO infrastructure dependencies, NO JPA annotations.
 * Represents a refresh token for JWT authentication.
 */
@Value
@Builder(toBuilder = true)
public class RefreshTokenDomain {

    Long id;

    @With
    String token;

    Long userId; // Reference to user by ID only (no direct object reference)

    @With
    LocalDateTime expiryDate;

    @With
    @Builder.Default
    boolean revoked = false;

    LocalDateTime createdAt;

    /**
     * Business rule validation.
     */
    public void validate() {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        if (token.length() > 500) {
            throw new IllegalArgumentException("Token must not exceed 500 characters");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }
    }

    /**
     * Checks if this token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Checks if this token is valid (not revoked and not expired).
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    /**
     * Checks if this token is revoked.
     */
    public boolean isRevoked() {
        return revoked;
    }

    /**
     * Creates a new instance with the token revoked.
     */
    public RefreshTokenDomain revoke() {
        return this.withRevoked(true);
    }
}
