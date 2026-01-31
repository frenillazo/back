package com.acainfo.security.verification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * EmailVerificationToken entity for email verification mechanism.
 * This is NOT a domain entity - it belongs to infrastructure (security).
 *
 * Stores tokens sent to users via email to verify their email address.
 */
@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID (foreign key to users table).
     * We use Long instead of relationship to avoid coupling with user module.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * The verification token string (UUID).
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    /**
     * Expiration date of the verification token.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Creation date.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Whether the token has been used (email verified).
     */
    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    /**
     * Check if token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token is valid (not expired and not used).
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }
}
