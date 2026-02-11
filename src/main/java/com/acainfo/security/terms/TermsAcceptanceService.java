package com.acainfo.security.terms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing terms and conditions acceptance.
 * Handles recording acceptances and checking if users have accepted the current version.
 *
 * To require re-acceptance after updating terms, simply change CURRENT_TERMS_VERSION.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TermsAcceptanceService {

    /**
     * Current version of the terms and conditions document.
     * Change this value when the document is updated to force re-acceptance.
     */
    public static final String CURRENT_TERMS_VERSION = "1.0";

    private final TermsAcceptanceRepository termsAcceptanceRepository;

    /**
     * Check if a user has accepted the current terms version.
     *
     * @param userId User ID
     * @return true if user has accepted the current version
     */
    @Transactional(readOnly = true)
    public boolean hasAcceptedCurrentTerms(Long userId) {
        return termsAcceptanceRepository.existsByUserIdAndTermsVersion(userId, CURRENT_TERMS_VERSION);
    }

    /**
     * Record a user's acceptance of the current terms version.
     *
     * @param userId    User ID
     * @param ipAddress Client IP address at the time of acceptance
     * @return The created TermsAcceptance record
     */
    @Transactional
    public TermsAcceptance acceptTerms(Long userId, String ipAddress) {
        // Check if already accepted (idempotent)
        if (hasAcceptedCurrentTerms(userId)) {
            log.info("User {} has already accepted terms version {}", userId, CURRENT_TERMS_VERSION);
            return termsAcceptanceRepository.findByUserIdOrderByAcceptedAtDesc(userId).getFirst();
        }

        TermsAcceptance acceptance = TermsAcceptance.builder()
                .userId(userId)
                .termsVersion(CURRENT_TERMS_VERSION)
                .acceptedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

        TermsAcceptance saved = termsAcceptanceRepository.save(acceptance);
        log.info("User {} accepted terms version {} from IP {}", userId, CURRENT_TERMS_VERSION, ipAddress);

        return saved;
    }

    /**
     * Get the current terms version string.
     *
     * @return Current terms version
     */
    public String getCurrentTermsVersion() {
        return CURRENT_TERMS_VERSION;
    }
}
