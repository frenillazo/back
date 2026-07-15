package com.acainfo.material.application.port.out;

import com.acainfo.material.domain.model.MaterialAiJob;

import java.util.Optional;

/**
 * Output port for MaterialAiJob persistence.
 */
public interface MaterialAiJobRepositoryPort {

    /**
     * Save or update a job.
     */
    MaterialAiJob save(MaterialAiJob job);

    /**
     * Find job by ID.
     */
    Optional<MaterialAiJob> findById(Long id);

    /**
     * Mark every PENDING/RUNNING job as FAILED with the given message.
     * Used on startup: a restart kills in-flight jobs (deploy.sh does docker restart).
     *
     * @return number of jobs marked as failed
     */
    int failInterruptedJobs(String errorMessage);
}
