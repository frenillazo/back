package acainfo.back.application.ports.in;

import acainfo.back.domain.model.*;

import java.util.List;

/**
 * Use case for retrieving groups.
 */
public interface GetGroupUseCase {

    /**
     * Gets a group by its ID.
     *
     * @param id the group ID
     * @return the group
     * @throws acainfo.back.domain.exception.GroupNotFoundException if not found
     */
    Group getGroupById(Long id);

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    List<Group> getAllGroups();

    /**
     * Gets all active groups.
     *
     * @return list of active groups
     */
    List<Group> getActiveGroups();

    /**
     * Gets groups by subject.
     *
     * @param subjectId the subject ID
     * @return list of groups
     */
    List<Group> getGroupsBySubject(Long subjectId);

    /**
     * Gets groups by teacher.
     *
     * @param teacherId the teacher ID
     * @return list of groups
     */
    List<Group> getGroupsByTeacher(Long teacherId);

    /**
     * Gets groups by status.
     *
     * @param status the group status
     * @return list of groups
     */
    List<Group> getGroupsByStatus(GroupStatus status);

    /**
     * Gets groups by type.
     *
     * @param type the group type
     * @return list of groups
     */
    List<Group> getGroupsByType(GroupType type);

    /**
     * Gets groups by period.
     *
     * @param period the academic period
     * @return list of groups
     */
    List<Group> getGroupsByPeriod(AcademicPeriod period);

    /**
     * Gets groups by classroom.
     *
     * @param classroom the classroom
     * @return list of groups
     */
    List<Group> getGroupsByClassroom(Classroom classroom);

    /**
     * Gets groups with available places.
     *
     * @return list of groups with available places
     */
    List<Group> getGroupsWithAvailablePlaces();
}
