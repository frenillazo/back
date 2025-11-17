package acainfo.back.infrastructure.adapters.out;

import acainfo.back.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Group entity.
 * Provides CRUD operations, custom queries, and dynamic filtering with Specifications.
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {

    /**
     * Find all groups by subject
     * @param subject the subject
     * @return list of groups
     */
    List<Group> findBySubject(Subject subject);

    /**
     * Find all groups by subject ID
     * @param subjectId the subject ID
     * @return list of groups
     */
    @Query("SELECT g FROM Group g WHERE g.subject.id = :subjectId")
    List<Group> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all groups by teacher
     * @param teacher the teacher
     * @return list of groups
     */
    List<Group> findByTeacher(User teacher);

    /**
     * Find all groups by teacher ID
     * @param teacherId the teacher ID
     * @return list of groups
     */
    @Query("SELECT g FROM Group g WHERE g.teacher.id = :teacherId")
    List<Group> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find all groups by status
     * @param status the group status
     * @return list of groups
     */
    List<Group> findByStatus(GroupStatus status);

    /**
     * Find all active groups
     * @return list of active groups
     */
    default List<Group> findAllActive() {
        return findByStatus(GroupStatus.ACTIVO);
    }

    /**
     * Find all groups by type
     * @param type the group type
     * @return list of groups
     */
    List<Group> findByType(GroupType type);

    /**
     * Find all groups by period
     * @param period the academic period
     * @return list of groups
     */
    List<Group> findByPeriod(AcademicPeriod period);

    /**
     * Find all groups by classroom
     * @param classroom the classroom
     * @return list of groups
     */
    List<Group> findByClassroom(Classroom classroom);

    /**
     * Find all groups with available places (not full)
     * @return list of groups with available places
     */
    @Query("SELECT g FROM Group g WHERE g.currentOccupancy < g.maxCapacity AND g.status = 'ACTIVO'")
    List<Group> findGroupsWithAvailablePlaces();

    /**
     * Find all full groups
     * @return list of full groups
     */
    @Query("SELECT g FROM Group g WHERE g.currentOccupancy >= g.maxCapacity OR g.status = 'COMPLETO'")
    List<Group> findFullGroups();

    /**
     * Find all groups by subject and period
     * @param subject the subject
     * @param period the academic period
     * @return list of groups
     */
    List<Group> findBySubjectAndPeriod(Subject subject, AcademicPeriod period);

    /**
     * Find all groups by subject ID and period
     * @param subjectId the subject ID
     * @param period the academic period
     * @return list of groups
     */
    @Query("SELECT g FROM Group g WHERE g.subject.id = :subjectId AND g.period = :period")
    List<Group> findBySubjectIdAndPeriod(@Param("subjectId") Long subjectId, @Param("period") AcademicPeriod period);

    /**
     * Find all active groups by subject
     * @param subject the subject
     * @return list of active groups
     */
    @Query("SELECT g FROM Group g WHERE g.subject = :subject AND g.status = 'ACTIVO'")
    List<Group> findActiveBySubject(@Param("subject") Subject subject);

    /**
     * Find all active groups by subject ID
     * @param subjectId the subject ID
     * @return list of active groups
     */
    @Query("SELECT g FROM Group g WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    List<Group> findActiveBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count groups by subject
     * @param subject the subject
     * @return count of groups
     */
    long countBySubject(Subject subject);

    /**
     * Count groups by subject ID
     * @param subjectId the subject ID
     * @return count of groups
     */
    @Query("SELECT COUNT(g) FROM Group g WHERE g.subject.id = :subjectId")
    long countBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count active groups by subject ID
     * @param subjectId the subject ID
     * @return count of active groups
     */
    @Query("SELECT COUNT(g) FROM Group g WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    long countActiveGroupsBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count groups by status
     * @param status the group status
     * @return count of groups
     */
    long countByStatus(GroupStatus status);

    /**
     * Count active groups
     * @return count of active groups
     */
    default long countActive() {
        return countByStatus(GroupStatus.ACTIVO);
    }

    /**
     * Find groups by teacher and status
     * @param teacher the teacher
     * @param status the status
     * @return list of groups
     */
    List<Group> findByTeacherAndStatus(User teacher, GroupStatus status);

    /**
     * Check if a subject has any active groups
     * @param subjectId the subject ID
     * @return true if has active groups, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g " +
           "WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    boolean hasActiveGroups(@Param("subjectId") Long subjectId);

    /**
     * Find all groups by type and period
     * @param type the group type
     * @param period the academic period
     * @return list of groups
     */
    List<Group> findByTypeAndPeriod(GroupType type, AcademicPeriod period);

    /**
     * Find all groups by classroom and status
     * @param classroom the classroom
     * @param status the status
     * @return list of groups
     */
    List<Group> findByClassroomAndStatus(Classroom classroom, GroupStatus status);
}
