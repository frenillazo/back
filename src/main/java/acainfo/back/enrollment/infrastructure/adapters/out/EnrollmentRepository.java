package acainfo.back.enrollment.infrastructure.adapters.out;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Enrollment entity.
 * Provides CRUD operations and custom queries for enrollment management.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments by student ID
     * @param studentId the student ID
     * @return list of enrollments
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find all enrollments by subject group ID
     * @param subjectGroupId the subject group ID
     * @return list of enrollments
     */
    @Query("SELECT e FROM Enrollment e WHERE e.subjectGroup.id = :subjectGroupId")
    List<Enrollment> findBySubjectGroupId(@Param("subjectGroupId") Long subjectGroupId);

    /**
     * Find all enrollments by status
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findByStatus(EnrollmentStatus status);

    /**
     * Find all enrollments by student ID and status
     * @param studentId the student ID
     * @param status the enrollment status
     * @return list of enrollments
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status);

    /**
     * Find all enrollments by subject group ID and status
     * @param subjectGroupId the subject group ID
     * @param status the enrollment status
     * @return list of enrollments
     */
    @Query("SELECT e FROM Enrollment e WHERE e.subjectGroup.id = :subjectGroupId AND e.status = :status")
    List<Enrollment> findBySubjectGroupIdAndStatus(
            @Param("subjectGroupId") Long subjectGroupId,
            @Param("status") EnrollmentStatus status);

    /**
     * Find enrollment by student ID and subject group ID
     * @param studentId the student ID
     * @param subjectGroupId the subject group ID
     * @return Optional containing the enrollment if found
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.subjectGroup.id = :subjectGroupId")
    Optional<Enrollment> findByStudentIdAndSubjectGroupId(
            @Param("studentId") Long studentId,
            @Param("subjectGroupId") Long subjectGroupId);

    /**
     * Count enrollments by status
     * @param status the enrollment status
     * @return count of enrollments
     */
    long countByStatus(EnrollmentStatus status);

    /**
     * Count enrollments by student ID
     * @param studentId the student ID
     * @return count of enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId")
    long countByStudentId(@Param("studentId") Long studentId);

    /**
     * Count active enrollments by student ID
     * @param studentId the student ID
     * @return count of active enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    long countActiveByStudentId(@Param("studentId") Long studentId);

    /**
     * Count enrollments by subject group ID
     * @param subjectGroupId the subject group ID
     * @return count of enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :subjectGroupId")
    long countBySubjectGroupId(@Param("subjectGroupId") Long subjectGroupId);

    /**
     * Count active enrollments by subject group ID
     * @param subjectGroupId the subject group ID
     * @return count of active enrollments
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :subjectGroupId AND e.status = 'ACTIVE'")
    long countActiveBySubjectGroupId(@Param("subjectGroupId") Long subjectGroupId);

    /**
     * Check if enrollment exists by student ID and subject group ID
     * @param studentId the student ID
     * @param subjectGroupId the subject group ID
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e " +
           "WHERE e.student.id = :studentId AND e.subjectGroup.id = :subjectGroupId")
    boolean existsByStudentIdAndSubjectGroupId(
            @Param("studentId") Long studentId,
            @Param("subjectGroupId") Long subjectGroupId);

    /**
     * Find all active enrollments
     * @return list of active enrollments
     */
    default List<Enrollment> findAllActive() {
        return findByStatus(EnrollmentStatus.ACTIVE);
    }
}
