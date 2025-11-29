package acainfo.back.subjectgroup.application.ports.in;

import acainfo.back.user.domain.exception.InvalidTeacherException;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subject.domain.exception.SubjectInactiveException;
import acainfo.back.subjectgroup.domain.exception.MaxGroupsPerSubjectException;

/**
 * Use case for creating a new subjectGroup.
 */
public interface CreateGroupUseCase {

    /**
     * Creates a new subjectGroup in the system.
     * Validates business rules:
     * - Maximum 3 groups per subject
     * - Subject must be active
     * - Teacher must have TEACHER role (if assigned)
     * - Capacity is set automatically based on classroom
     *
     * @param subjectGroup the subjectGroup to create
     * @return the created subjectGroup with generated ID
     * @throws MaxGroupsPerSubjectException if subject already has 3 groups
     * @throws SubjectInactiveException if subject is not active
     * @throws InvalidTeacherException if teacher is not valid
     */
    SubjectGroup createGroup(SubjectGroup subjectGroup);
}
