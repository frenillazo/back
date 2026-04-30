package com.acainfo.material.infrastructure.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time backfill on app startup: ensures every existing material has
 * non-null {@code visibility_enabled_at} and {@code download_enabled_at}
 * (filled with {@code uploaded_at} as a sensible default).
 *
 * Idempotent — only updates rows where the columns are still NULL.
 */
@Slf4j
@Component
public class MaterialTimestampBackfillRunner implements ApplicationRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        int visibilityFilled = em.createQuery(
                "UPDATE MaterialJpaEntity m " +
                        "SET m.visibilityEnabledAt = m.uploadedAt " +
                        "WHERE m.visibilityEnabledAt IS NULL"
        ).executeUpdate();

        int downloadFilled = em.createQuery(
                "UPDATE MaterialJpaEntity m " +
                        "SET m.downloadEnabledAt = m.uploadedAt " +
                        "WHERE m.downloadEnabledAt IS NULL"
        ).executeUpdate();

        if (visibilityFilled > 0 || downloadFilled > 0) {
            log.info("Material timestamp backfill: visibilityEnabledAt={}, downloadEnabledAt={}",
                    visibilityFilled, downloadFilled);
        }
    }
}
