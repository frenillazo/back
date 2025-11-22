package acainfo.back.subjectgroup.application.ports.in;

import acainfo.back.shared.domain.exception.InvalidTeacherException;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;

/**
 * Use case for updating an existing subjectGroup.
 * Works with SubjectGroupDomain (pure domain model)
 */
public interface UpdateGroupUseCase {

    /**
     * Updates an existing subjectGroup in the system.
     * Validates business rules similar to creation.
     *
     * @param id the subjectGroup ID to update
     * @param subjectGroup the updated subjectGroup data
     * @return the updated subjectGroup
     * @throws GroupNotFoundException if subjectGroup not found
     * @throws InvalidTeacherException if teacher is not valid
     */
    SubjectGroupDomain updateGroup(Long id, SubjectGroupDomain subjectGroup);

    /**
     * Assigns a teacher to a subjectGroup.
     *
     * @param groupId the subjectGroup ID
     * @param teacherId the teacher ID
     * @return the updated subjectGroup
     * @throws GroupNotFoundException if subjectGroup not found
     * @throws UserNotFoundException if teacher not found
     * @throws InvalidTeacherException if user is not a teacher
     */
    SubjectGroupDomain assignTeacher(Long groupId, Long teacherId);
}
