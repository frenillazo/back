package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.Enrollment;

/**
 * Use case for changing a student's enrollment to a different group.
 */
public interface ChangeGroupUseCase {

    /**
     * Changes a student's enrollment to a different group of the same subject.
     *
     * @param enrollmentId the current enrollment ID
     * @param newGroupId the new group ID
     * @return the new enrollment
     */
    Enrollment changeGroup(Long enrollmentId, Long newGroupId);
}
