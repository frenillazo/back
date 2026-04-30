package com.acainfo.group.infrastructure.adapter.out.persistence;

import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * One-time backfill on app startup: ensures every existing regular group has
 * non-null {@code start_date} and {@code end_date} (filled with sensible defaults
 * derived from {@code created_at}).
 *
 * <p>Two-deploy migration plan:</p>
 * <ol>
 *   <li><b>Deploy 1</b> (this version): the JPA mapping has {@code start_date} /
 *       {@code end_date} as <em>nullable</em>. Hibernate {@code ddl-auto=update}
 *       can add the columns to a non-empty {@code subject_groups} table without
 *       failing. This runner then populates the legacy NULL rows.
 *       New groups created via the API always have non-null values
 *       (validated in {@code GroupService}).</li>
 *   <li><b>Deploy 2</b>: tighten the mapping back to {@code @Column(nullable = false)}
 *       and run once on the prod DB:
 *       <pre>
 *       ALTER TABLE subject_groups ALTER COLUMN start_date SET NOT NULL;
 *       ALTER TABLE subject_groups ALTER COLUMN end_date SET NOT NULL;
 *       </pre>
 *   </li>
 * </ol>
 *
 * Idempotent — only updates rows where the columns are still NULL.
 * Portable — uses plain Java date arithmetic (no dialect-specific JPQL functions).
 */
@Slf4j
@Component
public class GroupDateBackfillRunner implements ApplicationRunner {

    /** Default duration applied to legacy groups without an explicit endDate. */
    private static final int DEFAULT_DURATION_MONTHS = 6;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        TypedQuery<SubjectGroupJpaEntity> q = em.createQuery(
                "SELECT g FROM SubjectGroupJpaEntity g " +
                        "WHERE g.startDate IS NULL OR g.endDate IS NULL",
                SubjectGroupJpaEntity.class
        );
        List<SubjectGroupJpaEntity> legacy = q.getResultList();
        if (legacy.isEmpty()) {
            return;
        }

        int filled = 0;
        for (SubjectGroupJpaEntity g : legacy) {
            LocalDate base = g.getCreatedAt() != null
                    ? g.getCreatedAt().toLocalDate()
                    : LocalDate.now();
            if (g.getStartDate() == null) {
                g.setStartDate(base);
            }
            if (g.getEndDate() == null) {
                g.setEndDate(base.plusMonths(DEFAULT_DURATION_MONTHS));
            }
            em.merge(g);
            filled++;
        }

        log.info("Group date backfill: populated start/end date on {} legacy groups " +
                        "(default duration {} months). On the next deploy, tighten the mapping " +
                        "to nullable=false and run: " +
                        "ALTER TABLE subject_groups ALTER COLUMN start_date SET NOT NULL; " +
                        "ALTER TABLE subject_groups ALTER COLUMN end_date SET NOT NULL;",
                filled, DEFAULT_DURATION_MONTHS);
    }
}
