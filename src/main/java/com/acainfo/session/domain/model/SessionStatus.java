package com.acainfo.session.domain.model;

/**
 * Status of a session in its lifecycle.
 *
 * <p>State transitions:</p>
 * <pre>
 * SCHEDULED ──┬──► IN_PROGRESS ──► COMPLETED
 *             │
 *             ├──► CANCELLED
 *             │
 *             └──► POSTPONED ──► SCHEDULED (new date)
 * </pre>
 */
public enum SessionStatus {
    /**
     * Session is scheduled and waiting to start.
     */
    SCHEDULED,

    /**
     * Session is currently in progress.
     */
    IN_PROGRESS,

    /**
     * Session has been completed successfully.
     */
    COMPLETED,

    /**
     * Session has been cancelled and will not take place.
     */
    CANCELLED,

    /**
     * Session has been postponed to a new date.
     */
    POSTPONED
}
