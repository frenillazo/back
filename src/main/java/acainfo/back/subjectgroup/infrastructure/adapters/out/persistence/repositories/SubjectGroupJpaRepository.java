package acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for SubjectGroup persistence
 * Works with SubjectGroupJpaEntity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface SubjectGroupJpaRepository extends JpaRepository<SubjectGroupJpaEntity, Long> {

    /**
     * Find all groups by subject
     * @param subject the subject
     * @return list of groups
     */
    List<SubjectGroupJpaEntity> findBySubject(SubjectJpaEntity subject);

    /**
     * Find all groups by subject ID
     * @param subjectId the subject ID
     * @return list of groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject.id = :subjectId")
    List<SubjectGroupJpaEntity> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all groups by teacher
     * @param teacher the teacher
     * @return list of groups
     */
    List<SubjectGroupJpaEntity> findByTeacher(User teacher);

    /**
     * Find all groups by teacher ID
     * @param teacherId the teacher ID
     * @return list of groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.teacher.id = :teacherId")
    List<SubjectGroupJpaEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find all groups by status
     * @param status the subjectGroup status
     * @return list of groups
     */
    List<SubjectGroupJpaEntity> findByStatus(GroupStatus status);

    /**
     * Find all active groups
     * @return list of active groups
     */
    default List<SubjectGroupJpaEntity> findAllActive() {
        return findByStatus(GroupStatus.ACTIVO);
    }

    /**
     * Find all groups by type
     * @param type the subjectGroup type
     * @return list of groups
     */
    List<SubjectGroupJpaEntity> findByType(GroupType type);

    /**
     * Find all groups by period
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroupJpaEntity> findByPeriod(AcademicPeriod period);

    /**
     * Find all groups with available places (not full)
     * @return list of groups with available places
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.currentOccupancy < g.maxCapacity AND g.status = 'ACTIVO'")
    List<SubjectGroupJpaEntity> findGroupsWithAvailablePlaces();

    /**
     * Find all full groups
     * @return list of full groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.currentOccupancy >= g.maxCapacity OR g.status = 'COMPLETO'")
    List<SubjectGroupJpaEntity> findFullGroups();

    /**
     * Find all groups by subject and period
     * @param subject the subject
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroupJpaEntity> findBySubjectAndPeriod(SubjectJpaEntity subject, AcademicPeriod period);

    /**
     * Find all groups by subject ID and period
     * @param subjectId the subject ID
     * @param period the academic period
     * @return list of groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject.id = :subjectId AND g.period = :period")
    List<SubjectGroupJpaEntity> findBySubjectIdAndPeriod(@Param("subjectId") Long subjectId, @Param("period") AcademicPeriod period);

    /**
     * Find all active groups by subject
     * @param subject the subject
     * @return list of active groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject = :subject AND g.status = 'ACTIVO'")
    List<SubjectGroupJpaEntity> findActiveBySubject(@Param("subject") SubjectJpaEntity subject);

    /**
     * Find all active groups by subject ID
     * @param subjectId the subject ID
     * @return list of active groups
     */
    @Query("SELECT g FROM SubjectGroup g WHERE g.subject.id = :subjectId AND g.status = 'ACTIVO'")
    List<SubjectGroupJpaEntity> findActiveBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count groups by subject
     * @param subject the subject
     * @return count of groups
     */
    long countBySubject(SubjectJpaEntity subject);

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
    List<SubjectGroupJpaEntity> findByTeacherAndStatus(User teacher, GroupStatus status);

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
    List<SubjectGroupJpaEntity> findByTypeAndPeriod(GroupType type, AcademicPeriod period);
}
