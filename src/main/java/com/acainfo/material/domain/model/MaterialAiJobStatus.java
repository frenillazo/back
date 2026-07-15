package com.acainfo.material.domain.model;

/**
 * Lifecycle of an AI LaTeX job: PENDING -> RUNNING -> COMPLETED | FAILED.
 * Jobs left PENDING/RUNNING by a server restart are marked FAILED on startup.
 */
public enum MaterialAiJobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
