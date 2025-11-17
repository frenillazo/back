package acainfo.back.application.ports.in;

import acainfo.back.domain.model.Group;

/**
 * Use case for updating an existing group.
 */
public interface UpdateGroupUseCase {

    /**
     * Updates an existing group in the system.
     * Validates business rules similar to creation.
     *
     * @param id the group ID to update
     * @param group the updated group data
     * @return the updated group
     * @throws acainfo.back.domain.exception.GroupNotFoundException if group not found
     * @throws acainfo.back.domain.exception.InvalidTeacherException if teacher is not valid
     */
    Group updateGroup(Long id, Group group);

    /**
     * Assigns a teacher to a group.
     *
     * @param groupId the group ID
     * @param teacherId the teacher ID
     * @return the updated group
     * @throws acainfo.back.domain.exception.GroupNotFoundException if group not found
     * @throws acainfo.back.domain.exception.UserNotFoundException if teacher not found
     * @throws acainfo.back.domain.exception.InvalidTeacherException if user is not a teacher
     */
    Group assignTeacher(Long groupId, Long teacherId);
}
