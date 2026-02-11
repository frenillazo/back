package com.acainfo.security.terms;

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
 * Entity that records each user's acceptance of terms and conditions.
 * This is NOT a domain entity - it belongs to infrastructure (security).
 *
 * Stores: who accepted, which version, when, and from which IP.
 * Required by GDPR Art. 7.1 to demonstrate that consent was given.
 */
@Entity
@Table(name = "terms_acceptances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermsAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID (conceptual FK to users table).
     * Uses Long instead of relationship to avoid coupling with user module.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Version of the terms document accepted (e.g., "1.0", "1.1").
     */
    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    /**
     * Timestamp when the user accepted the terms.
     */
    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    /**
     * IP address of the client at the time of acceptance.
     * Extracted from X-Forwarded-For (Cloudflare) or request.getRemoteAddr().
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
