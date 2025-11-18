package acainfo.back.subjectgroup.application.ports.in;

import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;

import java.util.List;

/**
 * Use case for retrieving groups.
 */
public interface GetGroupUseCase {

    /**
     * Gets a subjectGroup by its ID.
     *
     * @param id the subjectGroup ID
     * @return the subjectGroup
     * @throws GroupNotFoundException if not found
     */
    SubjectGroup getGroupById(Long id);

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    List<SubjectGroup> getAllGroups();

    /**
     * Gets all active groups.
     *
     * @return list of active groups
     */
    List<SubjectGroup> getActiveGroups();

    /**
     * Gets groups by subject.
     *
     * @param subjectId the subject ID
     * @return list of groups
     */
    List<SubjectGroup> getGroupsBySubject(Long subjectId);

    /**
     * Gets groups by teacher.
     *
     * @param teacherId the teacher ID
     * @return list of groups
     */
    List<SubjectGroup> getGroupsByTeacher(Long teacherId);

    /**
     * Gets groups by status.
     *
     * @param status the subjectGroup status
     * @return list of groups
     */
    List<SubjectGroup> getGroupsByStatus(GroupStatus status);

    /**
     * Gets groups by type.
     *
     * @param type the subjectGroup type
     * @return list of groups
     */
    List<SubjectGroup> getGroupsByType(GroupType type);

    /**
     * Gets groups by period.
     *
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroup> getGroupsByPeriod(AcademicPeriod period);

    /**
     * Gets groups with available places.
     *
     * @return list of groups with available places
     */
    List<SubjectGroup> getGroupsWithAvailablePlaces();
}
