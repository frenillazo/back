package acainfo.back.enrollment.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.enrollment.domain.model.AttendanceMode;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Enrollment persistence
 * Infrastructure layer - Spring Data JPA interface
 */
@Repository
public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    /**
     * Finds enrollments by student ID.
     */
    List<EnrollmentJpaEntity> findByStudentId(Long studentId);

    /**
     * Finds enrollments by student ID and status.
     */
    List<EnrollmentJpaEntity> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Finds enrollments by subject group ID.
     */
    List<EnrollmentJpaEntity> findBySubjectGroupId(Long subjectGroupId);

    /**
     * Finds enrollments by subject group ID and status.
     */
    List<EnrollmentJpaEntity> findBySubjectGroupIdAndStatus(Long subjectGroupId, EnrollmentStatus status);

    /**
     * Finds enrollments in waiting queue for a group, ordered by enrollment date (FIFO).
     */
    List<EnrollmentJpaEntity> findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(
            Long subjectGroupId,
            EnrollmentStatus status
    );

    /**
     * Checks if a student is enrolled in a specific group with given status.
     */
    boolean existsByStudentIdAndSubjectGroupIdAndStatus(
            Long studentId,
            Long subjectGroupId,
            EnrollmentStatus status
    );

    /**
     * Counts enrollments by student ID and status.
     */
    long countByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Counts enrollments by subject group ID and status.
     */
    long countBySubjectGroupIdAndStatus(Long subjectGroupId, EnrollmentStatus status);

    /**
     * Counts presential enrollments for a group.
     * These are enrollments with ACTIVO status and PRESENCIAL mode.
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :groupId " +
           "AND e.status = 'ACTIVO' AND e.attendanceMode = 'PRESENCIAL'")
    long countPresentialEnrollments(@Param("groupId") Long groupId);

    /**
     * Counts online enrollments for a group.
     * These are enrollments with ACTIVO status and ONLINE mode.
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :groupId " +
           "AND e.status = 'ACTIVO' AND e.attendanceMode = 'ONLINE'")
    long countOnlineEnrollments(@Param("groupId") Long groupId);

    /**
     * Finds all active enrollments for a student.
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVO'")
    List<EnrollmentJpaEntity> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Finds enrollments by attendance mode.
     */
    List<EnrollmentJpaEntity> findBySubjectGroupIdAndAttendanceMode(Long subjectGroupId, AttendanceMode mode);

    /**
     * Finds enrollment by student ID and group ID.
     */
    Optional<EnrollmentJpaEntity> findByStudentIdAndSubjectGroupId(Long studentId, Long subjectGroupId);

    /**
     * Checks if an enrollment exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Finds all enrollments by status.
     */
    List<EnrollmentJpaEntity> findByStatus(EnrollmentStatus status);
}
