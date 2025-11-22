package acainfo.back.subjectgroup.application.ports.in;

import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;

import java.util.List;

/**
 * Use case for retrieving groups.
 * Works with SubjectGroupDomain (pure domain model)
 */
public interface GetGroupUseCase {

    /**
     * Gets a subjectGroup by its ID.
     *
     * @param id the subjectGroup ID
     * @return the subjectGroup
     * @throws GroupNotFoundException if not found
     */
    SubjectGroupDomain getGroupById(Long id);

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    List<SubjectGroupDomain> getAllGroups();

    /**
     * Gets all active groups.
     *
     * @return list of active groups
     */
    List<SubjectGroupDomain> getActiveGroups();

    /**
     * Gets groups by subject.
     *
     * @param subjectId the subject ID
     * @return list of groups
     */
    List<SubjectGroupDomain> getGroupsBySubject(Long subjectId);

    /**
     * Gets groups by teacher.
     *
     * @param teacherId the teacher ID
     * @return list of groups
     */
    List<SubjectGroupDomain> getGroupsByTeacher(Long teacherId);

    /**
     * Gets groups by status.
     *
     * @param status the subjectGroup status
     * @return list of groups
     */
    List<SubjectGroupDomain> getGroupsByStatus(GroupStatus status);

    /**
     * Gets groups by type.
     *
     * @param type the subjectGroup type
     * @return list of groups
     */
    List<SubjectGroupDomain> getGroupsByType(GroupType type);

    /**
     * Gets groups by period.
     *
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroupDomain> getGroupsByPeriod(AcademicPeriod period);

    /**
     * Gets groups with available places.
     *
     * @return list of groups with available places
     */
    List<SubjectGroupDomain> getGroupsWithAvailablePlaces();
}
