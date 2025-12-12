package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.domain.model.Enrollment;

import java.util.List;

/**
 * Use case for managing the waiting list.
 * Input port defining the contract for waiting list operations.
 *
 * <p>The waiting list follows FIFO order for promotions.</p>
 */
public interface WaitingListUseCase {

    /**
     * Get the waiting list for a group, ordered by position (FIFO).
     *
     * @param groupId Group ID
     * @return List of enrollments in waiting list, ordered by position
     */
    List<Enrollment> getWaitingListByGroupId(Long groupId);

    /**
     * Get all waiting list positions for a student across all groups.
     *
     * @param studentId Student ID
     * @return List of enrollments in waiting list
     */
    List<Enrollment> getWaitingListByStudentId(Long studentId);

    /**
     * Remove a student from a waiting list.
     *
     * @param enrollmentId Enrollment ID (must be in WAITING_LIST status)
     * @return The updated enrollment with WITHDRAWN status
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException if not in waiting list
     */
    Enrollment leaveWaitingList(Long enrollmentId);

    /**
     * Promote the next student from waiting list to active enrollment.
     * Called automatically when a seat becomes available.
     *
     * @param groupId Group ID
     * @return The promoted enrollment, or null if waiting list is empty
     */
    Enrollment promoteNextFromWaitingList(Long groupId);
}
