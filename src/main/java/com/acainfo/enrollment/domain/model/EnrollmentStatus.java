package com.acainfo.enrollment.domain.model;

/**
 * Status of a student enrollment in a subject group.
 *
 * <p>State transitions:</p>
 * <pre>
 * ACTIVE ──┬──► WITHDRAWN (student leaves voluntarily)
 *          │
 *          └──► COMPLETED (course finished successfully)
 *
 * WAITING_LIST ──┬──► ACTIVE (seat becomes available)
 *                │
 *                └──► WITHDRAWN (student leaves queue)
 * </pre>
 */
public enum EnrollmentStatus {

    /**
     * Student is actively enrolled in the group.
     */
    ACTIVE,

    /**
     * Student is on the waiting list for the group.
     */
    WAITING_LIST,

    /**
     * Student has withdrawn from the group.
     */
    WITHDRAWN,

    /**
     * Student has successfully completed the course.
     */
    COMPLETED
}
