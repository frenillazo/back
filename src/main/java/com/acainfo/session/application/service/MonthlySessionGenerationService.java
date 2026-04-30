package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.application.port.in.GenerateSessionsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Scheduled job that generates the regular sessions of the current month for every
 * active (OPEN) regular group on day 1 of each month at 02:00.
 *
 * <p>The underlying {@link GenerateSessionsUseCase} is idempotent (it skips schedules
 * that already produced a session for a given date), so running this job twice in the
 * same month does not duplicate anything.</p>
 *
 * <p>For each group, the effective end date is capped by {@code group.endDate} so we
 * never create sessions past the group's lifespan.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlySessionGenerationService {

    private final GroupRepositoryPort groupRepositoryPort;
    private final GenerateSessionsUseCase generateSessionsUseCase;

    /**
     * Cron: day 1 of each month at 02:00. Configurable via
     * {@code app.session.monthly-generation.cron}.
     */
    @Scheduled(cron = "${app.session.monthly-generation.cron:0 0 2 1 * *}")
    @Transactional
    public void runScheduled() {
        runForCurrentMonth();
    }

    /**
     * Public entry point usable from a manual REST trigger / tests.
     */
    @Transactional
    public RunSummary runForCurrentMonth() {
        YearMonth month = YearMonth.now();
        LocalDate startOfMonth = month.atDay(1);
        LocalDate endOfMonth = month.atEndOfMonth();

        log.info("Monthly session generation starting for {} (range {} - {})",
                month, startOfMonth, endOfMonth);

        List<SubjectGroup> activeGroups = groupRepositoryPort.findAll().stream()
                .filter(g -> g.getStatus() == GroupStatus.OPEN)
                .toList();

        int processed = 0;
        int skippedEnded = 0;
        int totalCreated = 0;
        int errored = 0;

        for (SubjectGroup g : activeGroups) {
            processed++;

            LocalDate effectiveEnd = g.getEndDate() != null && g.getEndDate().isBefore(endOfMonth)
                    ? g.getEndDate()
                    : endOfMonth;

            if (g.getEndDate() != null && startOfMonth.isAfter(g.getEndDate())) {
                skippedEnded++;
                continue; // group is already past its end date
            }

            try {
                int created = generateSessionsUseCase
                        .generate(new GenerateSessionsCommand(g.getId(), startOfMonth, effectiveEnd))
                        .size();
                totalCreated += created;
            } catch (Exception e) {
                errored++;
                log.error("Monthly generation failed for group {}: {}", g.getId(), e.getMessage());
            }
        }

        log.info("Monthly session generation completed for {}: processed={}, skippedEnded={}, created={}, errored={}",
                month, processed, skippedEnded, totalCreated, errored);

        return new RunSummary(month, processed, skippedEnded, totalCreated, errored);
    }

    /** Summary returned by the manual run endpoint for visibility. */
    public record RunSummary(
            YearMonth month,
            int processed,
            int skippedEnded,
            int created,
            int errored
    ) {
    }
}
