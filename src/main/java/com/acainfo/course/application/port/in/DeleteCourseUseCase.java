package com.acainfo.course.application.port.in;

import com.acainfo.course.domain.model.Course;

/**
 * Use case for deleting groups.
 * Input port defining the contract for group deletion and cancellation.
 */
public interface DeleteCourseUseCase {

    /**
     * Delete a group (hard delete).
     * Note: Should verify that no enrollments are associated.
     *
     * @param id Group ID
     */
    void delete(Long id);

    /**
     * Cancel a group (soft delete).
     * Sets status to CANCELLED.
     *
     * @param id Group ID
     * @return The cancelled group
     */
    Course cancel(Long id);
}
