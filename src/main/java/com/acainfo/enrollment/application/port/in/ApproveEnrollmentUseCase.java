package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.domain.model.Enrollment;

/**
 * Use case for approving enrollment requests.
 * Teachers can approve enrollment requests for their groups.
 * Admins can approve any enrollment request.
 */
public interface ApproveEnrollmentUseCase {

    /**
     * Approve an enrollment request.
     * If seats are available, enrollment becomes ACTIVE.
     * If no seats available, enrollment goes to WAITING_LIST.
     *
     * @param enrollmentId ID of the enrollment to approve
     * @param approverUserId ID of the user approving (teacher or admin)
     * @return Updated enrollment
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if enrollment not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException if enrollment is not pending
     * @throws com.acainfo.enrollment.domain.exception.UnauthorizedApprovalException if approver is not authorized
     */
    Enrollment approve(Long enrollmentId, Long approverUserId);
}
