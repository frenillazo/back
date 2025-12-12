package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.application.dto.ChangeGroupCommand;
import com.acainfo.enrollment.domain.model.Enrollment;

/**
 * Use case for changing a student's enrollment to a different group.
 * Input port defining the contract for group changes.
 *
 * <p>Typically used for moving between parallel groups of the same subject.</p>
 */
public interface ChangeGroupUseCase {

    /**
     * Change a student's enrollment to a different group.
     *
     * @param command Change group data (enrollmentId, newGroupId)
     * @return The updated enrollment with new group
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if enrollment not found
     * @throws com.acainfo.group.domain.exception.GroupNotFoundException if new group not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException if not active
     * @throws com.acainfo.enrollment.domain.exception.GroupFullException if new group has no seats
     */
    Enrollment changeGroup(ChangeGroupCommand command);
}
