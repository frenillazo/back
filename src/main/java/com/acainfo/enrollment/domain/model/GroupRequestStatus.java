package com.acainfo.enrollment.domain.model;

/**
 * Status of a request for creating a new subject group.
 *
 * <p>State transitions:</p>
 * <pre>
 * PENDING ──┬──► APPROVED (admin approves, min 8 supporters)
 *           │
 *           ├──► REJECTED (admin rejects)
 *           │
 *           └──► EXPIRED (deadline passed without enough support)
 * </pre>
 */
public enum GroupRequestStatus {

    /**
     * Request is pending approval, waiting for supporters.
     */
    PENDING,

    /**
     * Request has been approved by admin and group will be created.
     */
    APPROVED,

    /**
     * Request has been rejected by admin.
     */
    REJECTED,

    /**
     * Request expired without reaching minimum supporters.
     */
    EXPIRED
}
