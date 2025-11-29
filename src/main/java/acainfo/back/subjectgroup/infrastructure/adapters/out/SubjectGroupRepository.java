package acainfo.back.subjectgroup.infrastructure.adapters.out;

import acainfo.back.user.domain.model.User;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for SubjectGroup entity.
 * Provides CRUD operations, custom queries, and dynamic filtering with Specifications.
 */
@Repository
public interface SubjectGroupRepository extends JpaRepository<SubjectGroup, Long>, JpaSpecificationExecutor<SubjectGroup> {

    /**
     * Find all groups by subject
     * @param subject the subject
     * @return list of groups
     */
    List<SubjectGroup> findBySubject(Subject subject);

    /**
     * Find all groups by subject ID
     * @param subjectId the subject ID
     * @return list of groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject.id = :subjectId")
    List<SubjectGroup> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all groups by teacher
     * @param teacher the teacher
     * @return list of groups
     */
    List<SubjectGroup> findByTeacher(User teacher);

    /**
     * Find all groups by teacher ID
     * @param teacherId the teacher ID
     * @return list of groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.teacher.id = :teacherId")
    List<SubjectGroup> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find all groups by status
     * @param status the subjectGroup status
     * @return list of groups
     */
    List<SubjectGroup> findByStatus(GroupStatus status);

    /**
     * Find all active groups
     * @return list of active groups
     */
    default List<SubjectGroup> findAllActive() {
        return findByStatus(GroupStatus.ACTIVO);
    }

    /**
     * Find all groups by type
     * @param type the subjectGroup type
     * @return list of groups
     */
    List<SubjectGroup> findByType(GroupType type);

    /**
     * Find all groups by period
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroup> findByPeriod(AcademicPeriod period);

    /**
     * Find all groups with available places (not full)
     * @return list of groups with available places
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.currentOccupancy < g.maxCapacity AND g.status = 'ACTIVO'")
    List<SubjectGroup> findGroupsWithAvailablePlaces();

    /**
     * Find all full groups
     * @return list of full groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.currentOccupancy >= g.maxCapacity OR g.status = 'COMPLETO'")
    List<SubjectGroup> findFullGroups();

    /**
     * Find all groups by subject and period
     * @param subject the subject
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroup> findBySubjectAndPeriod(Subject subject, AcademicPeriod period);

    /**
     * Find all groups by subject ID and period
     * @param subjectId the subject ID
     * @param period the academic period
     * @return list of groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject.id = :subjectId AND g.period = :period")
    List<SubjectGroup> findBySubjectIdAndPeriod(@Param("subjectId") Long subjectId, @Param("period") AcademicPeriod period);

    /**
     * Find all active groups by subject
     * @param subject the subject
     * @return list of active groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject = :subject AND g.status = 'ACTIVO'")
    List<SubjectGroup> findActiveBySubject(@Param("subject") Subject subject);

    /**
     * Find all active groups by subject ID
     * @param subjectId the subject ID
     * @return list of active groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    List<SubjectGroup> findActiveBySubjectId(@Param("subjectId") Long subjectId);

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
    @Query("SELECT COUNT(g) FROM SubjectGroup g WHERE g.subject.id = :subjectId")
    long countBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count active groups by subject ID
     * @param subjectId the subject ID
     * @return count of active groups
     */
    @Query("SELECT COUNT(g) FROM SubjectGroup g WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    long countActiveGroupsBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count groups by status
     * @param status the subjectGroup status
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
    List<SubjectGroup> findByTeacherAndStatus(User teacher, GroupStatus status);

    /**
     * Check if a subject has any active groups
     * @param subjectId the subject ID
     * @return true if has active groups, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM SubjectGroup g " +
           "WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    boolean hasActiveGroups(@Param("subjectId") Long subjectId);

    /**
     * Find all groups by type and period
     * @param type the subjectGroup type
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroup> findByTypeAndPeriod(GroupType type, AcademicPeriod period);
}
