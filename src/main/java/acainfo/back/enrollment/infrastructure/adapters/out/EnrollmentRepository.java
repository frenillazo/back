package acainfo.back.enrollment.infrastructure.adapters.out;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Enrollment entity.
 * Provides CRUD operations and custom queries for enrollment management.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments by student ID
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find enrollments by student ID and status
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") EnrollmentStatus status);

    /**
     * Find all enrollments by subject group ID
     */
    @Query("SELECT e FROM Enrollment e WHERE e.subjectGroup.id = :groupId")
    List<Enrollment> findBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Find enrollments by subject group ID and status
     */
    @Query("SELECT e FROM Enrollment e WHERE e.subjectGroup.id = :groupId AND e.status = :status")
    List<Enrollment> findBySubjectGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") EnrollmentStatus status);

    /**
     * Find enrollments in waiting queue for a group, ordered by enrollment date (FIFO)
     */
    @Query("SELECT e FROM Enrollment e WHERE e.subjectGroup.id = :groupId AND e.status = :status ORDER BY e.enrollmentDate ASC")
    List<Enrollment> findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(
        @Param("groupId") Long groupId,
        @Param("status") EnrollmentStatus status
    );

    /**
     * Check if a student is enrolled in a specific group with given status
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e " +
           "WHERE e.student.id = :studentId AND e.subjectGroup.id = :groupId AND e.status = :status")
    boolean existsByStudentIdAndSubjectGroupIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("groupId") Long groupId,
        @Param("status") EnrollmentStatus status
    );

    /**
     * Count enrollments by student ID and status
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") EnrollmentStatus status);

    /**
     * Count enrollments by subject group ID and status
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :groupId AND e.status = :status")
    long countBySubjectGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") EnrollmentStatus status);

    /**
     * Count presential enrollments for a group (ACTIVO + PRESENCIAL)
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :groupId " +
           "AND e.status = 'ACTIVO' AND e.attendanceMode = 'PRESENCIAL'")
    long countPresentialEnrollments(@Param("groupId") Long groupId);

    /**
     * Count online enrollments for a group (ACTIVO + ONLINE)
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.subjectGroup.id = :groupId " +
           "AND e.status = 'ACTIVO' AND e.attendanceMode = 'ONLINE'")
    long countOnlineEnrollments(@Param("groupId") Long groupId);

    /**
     * Find all active enrollments for a student
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVO'")
    List<Enrollment> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Find enrollments by subject group ID and attendance mode
     */
    @Query("SELECT e FROM Enrollment e WHERE e.subjectGroup.id = :groupId AND e.attendanceMode = :mode")
    List<Enrollment> findBySubjectGroupIdAndAttendanceMode(
        @Param("groupId") Long groupId,
        @Param("mode") AttendanceMode mode
    );

    /**
     * Find enrollment by student ID and group ID
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.subjectGroup.id = :groupId")
    Optional<Enrollment> findByStudentIdAndSubjectGroupId(
        @Param("studentId") Long studentId,
        @Param("groupId") Long groupId
    );

    /**
     * Find all enrollments by status
     */
    List<Enrollment> findByStatus(EnrollmentStatus status);

    /**
     * Find all active enrollments
     */
    default List<Enrollment> findAllActive() {
        return findByStatus(EnrollmentStatus.ACTIVO);
    }

    /**
     * Find all enrollments in waiting queue
     */
    default List<Enrollment> findAllWaiting() {
        return findByStatus(EnrollmentStatus.EN_ESPERA);
    }
}
