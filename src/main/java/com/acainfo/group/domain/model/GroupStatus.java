package com.acainfo.group.domain.model;

/**
 * Status of a subject group.
 * Represents the current state of the group in its lifecycle.
 */
public enum GroupStatus {
    /**
     * Group is open for enrollments.
     */
    OPEN,

    /**
     * Group is closed (no more enrollments allowed).
     */
    CLOSED,

    /**
     * Group has been cancelled (will not take place).
     */
    CANCELLED
}
