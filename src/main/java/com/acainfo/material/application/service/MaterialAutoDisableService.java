package com.acainfo.material.application.service;

import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.model.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled job that auto-disables materials that have been visible AND downloadable
 * for at least {@code app.material.auto-disable.threshold-days} days.
 *
 * <p>The goal is to force the administrator to periodically review which materials
 * remain public, instead of leaving them indefinitely available.</p>
 *
 * <p>Same pattern as
 * {@link com.acainfo.enrollment.application.service.EnrollmentExpirationService}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialAutoDisableService {

    private final MaterialRepositoryPort materialRepository;

    @Value("${app.material.auto-disable.threshold-days:3}")
    private int thresholdDays;

    /**
     * Runs daily at 03:00 (server time). Configurable via {@code app.material.auto-disable.cron}.
     */
    @Scheduled(cron = "${app.material.auto-disable.cron:0 0 3 * * *}")
    @Transactional
    public void disableExpiredMaterials() {
        log.info("Running material auto-disable job (threshold={} days)...", thresholdDays);

        List<Material> expired = materialRepository.findExpiredActiveMaterials(thresholdDays);
        if (expired.isEmpty()) {
            log.debug("No materials matched the auto-disable criteria");
            return;
        }

        List<Long> ids = expired.stream().map(Material::getId).toList();

        // Set visible=false and downloadDisabled=true. We do NOT update the *EnabledAt
        // timestamps here: when the admin reactivates them later, the corresponding
        // service will reset them.
        int hidden = materialRepository.batchUpdateVisibility(ids, false, null);
        int blocked = materialRepository.batchUpdateDownloadDisabled(ids, true, null);

        log.info("Material auto-disable job completed. Affected {} materials (hidden={}, downloadDisabled={})",
                expired.size(), hidden, blocked);
    }

    /**
     * Manual trigger (for tests / admin endpoint).
     */
    @Transactional
    public int runNow() {
        disableExpiredMaterials();
        return materialRepository.findExpiredActiveMaterials(thresholdDays).size();
    }
}
