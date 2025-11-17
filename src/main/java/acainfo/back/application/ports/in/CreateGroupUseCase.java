package acainfo.back.application.ports.in;

import acainfo.back.domain.model.Group;

/**
 * Use case for creating a new group.
 */
public interface CreateGroupUseCase {

    /**
     * Creates a new group in the system.
     * Validates business rules:
     * - Maximum 3 groups per subject
     * - Subject must be active
     * - Teacher must have TEACHER role (if assigned)
     * - Capacity is set automatically based on classroom
     *
     * @param group the group to create
     * @return the created group with generated ID
     * @throws acainfo.back.domain.exception.MaxGroupsPerSubjectException if subject already has 3 groups
     * @throws acainfo.back.domain.exception.SubjectInactiveException if subject is not active
     * @throws acainfo.back.domain.exception.InvalidTeacherException if teacher is not valid
     */
    Group createGroup(Group group);
}
