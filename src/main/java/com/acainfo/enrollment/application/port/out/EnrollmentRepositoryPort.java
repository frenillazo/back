package com.acainfo.enrollment.application.port.out;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Enrollment persistence.
 * Defines the contract for Enrollment repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface EnrollmentRepositoryPort {

    /**
     * Save or update an enrollment.
     *
     * @param enrollment Domain enrollment to persist
     * @return Persisted enrollment with ID
     */
    Enrollment save(Enrollment enrollment);

    /**
     * Find enrollment by ID.
     *
     * @param id Enrollment ID
     * @return Optional containing the enrollment if found
     */
    Optional<Enrollment> findById(Long id);

    /**
     * Find enrollments with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of enrollments matching filters
     */
    Page<Enrollment> findWithFilters(EnrollmentFilters filters);

    /**
     * Find all enrollments for a student.
     *
     * @param studentId Student ID
     * @return List of enrollments
     */
    List<Enrollment> findByStudentId(Long studentId);

    /**
     * Find all enrollments for a group.
     *
     * @param groupId Group ID
     * @return List of enrollments
     */
    List<Enrollment> findByGroupId(Long groupId);

    /**
     * Find enrollments by student and status.
     *
     * @param studentId Student ID
     * @param status Enrollment status
     * @return List of enrollments
     */
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Find enrollments by student and multiple statuses.
     *
     * @param studentId Student ID
     * @param statuses List of enrollment statuses
     * @return List of enrollments
     */
    List<Enrollment> findByStudentIdAndStatusIn(Long studentId, List<EnrollmentStatus> statuses);

    /**
     * Find enrollments by group and status.
     *
     * @param groupId Group ID
     * @param status Enrollment status
     * @return List of enrollments
     */
    List<Enrollment> findByGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    /**
     * Find a specific enrollment by student and group.
     *
     * @param studentId Student ID
     * @param groupId Group ID
     * @return Optional containing the enrollment if found
     */
    Optional<Enrollment> findByStudentIdAndGroupId(Long studentId, Long groupId);

    /**
     * Check if student is already enrolled (active or waiting) in a group.
     *
     * @param studentId Student ID
     * @param groupId Group ID
     * @return true if student is enrolled or on waiting list
     */
    boolean existsActiveOrWaitingEnrollment(Long studentId, Long groupId);

    /**
     * Check if student is already enrolled (active, waiting, or pending approval) in a group.
     *
     * @param studentId Student ID
     * @param groupId Group ID
     * @return true if student has any active enrollment request
     */
    boolean existsActiveOrWaitingOrPendingEnrollment(Long studentId, Long groupId);

    /**
     * Find all pending approval enrollments for groups taught by a specific teacher.
     *
     * @param teacherId Teacher user ID
     * @param groupIds List of group IDs taught by the teacher
     * @return List of enrollments pending approval
     */
    List<Enrollment> findPendingApprovalByGroupIds(List<Long> groupIds);

    /**
     * Find all expired pending enrollments (older than specified hours).
     *
     * @param hoursOld Number of hours since enrollment request
     * @return List of expired enrollments
     */
    List<Enrollment> findExpiredPendingEnrollments(int hoursOld);

    /**
     * Count active enrollments for a group.
     *
     * @param groupId Group ID
     * @return Number of active enrollments
     */
    long countActiveByGroupId(Long groupId);

    /**
     * Find waiting list for a group, ordered by position (FIFO).
     *
     * @param groupId Group ID
     * @return List of enrollments in waiting list order
     */
    List<Enrollment> findWaitingListByGroupId(Long groupId);

    /**
     * Get the next waiting list position for a group.
     *
     * @param groupId Group ID
     * @return Next position number
     */
    int getNextWaitingListPosition(Long groupId);

    /**
     * Decrement waiting list positions after a student leaves.
     *
     * @param groupId Group ID
     * @param position Position of student who left
     */
    void decrementWaitingListPositionsAfter(Long groupId, int position);

    /**
     * Delete an enrollment by ID.
     *
     * @param id Enrollment ID
     */
    void delete(Long id);
}
