package com.acainfo.enrollment.domain.model;

/**
 * Mode of attendance for an enrolled student.
 *
 * <p>Business rule: Students enrolled in 2+ subjects can attend online
 * when no physical seats are available.</p>
 */
public enum AttendanceMode {

    /**
     * Student attends classes in person at the physical classroom.
     * Default mode when seats are available.
     */
    IN_PERSON,

    /**
     * Student attends classes remotely via streaming.
     * Available for students enrolled in 2+ subjects when no physical seats.
     */
    ONLINE
}
