package com.acainfo.material.application.service;

import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A restart kills in-flight AI jobs (deploy.sh back does docker restart):
 * on startup, any job left PENDING/RUNNING can never finish, so it is marked
 * FAILED with a visible reason and the admin can relaunch it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialAiJobOrphanCleaner {

    static final String ORPHAN_ERROR_MESSAGE = "Interrumpido por reinicio del servidor";

    private final MaterialAiJobRepositoryPort jobRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void failOrphanJobs() {
        int failed = jobRepository.failInterruptedJobs(ORPHAN_ERROR_MESSAGE);
        if (failed > 0) {
            log.warn("{} job(s) de IA huérfanos marcados como FAILED tras el reinicio", failed);
        }
    }
}
