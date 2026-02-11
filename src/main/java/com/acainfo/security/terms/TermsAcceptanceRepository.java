package com.acainfo.security.terms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for TermsAcceptance.
 */
@Repository
public interface TermsAcceptanceRepository extends JpaRepository<TermsAcceptance, Long> {

    /**
     * Check if a user has accepted a specific terms version.
     */
    boolean existsByUserIdAndTermsVersion(Long userId, String termsVersion);

    /**
     * Get all acceptances for a user, ordered by most recent first.
     * Useful for audit purposes.
     */
    List<TermsAcceptance> findByUserIdOrderByAcceptedAtDesc(Long userId);
}
