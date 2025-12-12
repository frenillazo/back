package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.domain.model.Enrollment;

/**
 * Use case for withdrawing from enrollments.
 * Input port defining the contract for enrollment withdrawal.
 *
 * <p>When a student withdraws from an active enrollment,
 * the first student in the waiting list is automatically promoted.</p>
 */
public interface WithdrawEnrollmentUseCase {

    /**
     * Withdraw a student from their enrollment.
     * If the enrollment was ACTIVE, the first student in waiting list is promoted.
     *
     * @param enrollmentId Enrollment ID to withdraw
     * @return The updated enrollment with WITHDRAWN status
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException if already withdrawn/completed
     */
    Enrollment withdraw(Long enrollmentId);
}
