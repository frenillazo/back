package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.domain.model.Enrollment;

/**
 * Use case for rejecting enrollment requests.
 * Teachers can reject enrollment requests for their groups.
 * Admins can reject any enrollment request.
 */
public interface RejectEnrollmentUseCase {

    /**
     * Reject an enrollment request.
     *
     * @param enrollmentId ID of the enrollment to reject
     * @param rejecterUserId ID of the user rejecting (teacher or admin)
     * @param reason Optional reason for rejection
     * @return Updated enrollment with REJECTED status
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if enrollment not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException if enrollment is not pending
     * @throws com.acainfo.enrollment.domain.exception.UnauthorizedApprovalException if rejecter is not authorized
     */
    Enrollment reject(Long enrollmentId, Long rejecterUserId, String reason);
}
