package com.acainfo.enrollment.domain.model;

/**
 * Status of a student enrollment in a subject group.
 *
 * <p>State transitions:</p>
 * <pre>
 * PENDING_APPROVAL ──┬──► ACTIVE (teacher approves, seats available)
 *                    │
 *                    ├──► WAITING_LIST (teacher approves, no seats)
 *                    │
 *                    ├──► REJECTED (teacher rejects)
 *                    │
 *                    └──► EXPIRED (2 days timeout without response)
 *
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
     * Enrollment is pending teacher approval.
     * Initial state when student requests to enroll.
     */
    PENDING_APPROVAL,

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
    COMPLETED,

    /**
     * Enrollment request was rejected by the teacher.
     */
    REJECTED,

    /**
     * Enrollment request expired (teacher didn't respond within 2 days).
     */
    EXPIRED
}
