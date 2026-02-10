package com.acainfo.enrollment.application.port.out;

/**
 * Output port for automatic reservation management.
 * Implemented by reservation module to maintain hexagonal architecture.
 * Enrollment module depends on this interface, not the reservation implementation.
 */
public interface AutoReservationPort {

    /**
     * Generate reservations for a student in all future scheduled sessions of their group.
     * Called when enrollment is approved or promoted from waiting list.
     *
     * @param studentId    Student's user ID
     * @param groupId      Group ID to find future sessions
     * @param enrollmentId Enrollment ID for traceability
     */
    void generateForNewEnrollment(Long studentId, Long groupId, Long enrollmentId);

    /**
     * Cancel all future confirmed reservations for a student in a group.
     * Called when enrollment is withdrawn.
     *
     * @param studentId Student's user ID
     * @param groupId   Group ID to find future sessions
     */
    void cancelFutureReservations(Long studentId, Long groupId);
}
