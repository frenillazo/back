package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.domain.model.Enrollment;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Use case for retrieving enrollments.
 * Input port defining the contract for enrollment queries.
 */
public interface GetEnrollmentUseCase {

    /**
     * Get an enrollment by ID.
     *
     * @param id Enrollment ID
     * @return The enrollment
     * @throws com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException if not found
     */
    Enrollment getById(Long id);

    /**
     * Find enrollments with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of enrollments matching the filters
     */
    Page<Enrollment> findWithFilters(EnrollmentFilters filters);

    /**
     * Get all active enrollments for a student.
     *
     * @param studentId Student ID
     * @return List of active enrollments
     */
    List<Enrollment> findActiveByStudentId(Long studentId);

    /**
     * Get all active and pending enrollments for a student.
     * Includes ACTIVE, WAITING_LIST, and PENDING_APPROVAL statuses.
     *
     * @param studentId Student ID
     * @return List of active and pending enrollments
     */
    List<Enrollment> findActiveAndPendingByStudentId(Long studentId);

    /**
     * Get all active enrollments for a group.
     *
     * @param groupId Group ID
     * @return List of active enrollments
     */
    List<Enrollment> findActiveByGroupId(Long groupId);
}
