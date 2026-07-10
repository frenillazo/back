package com.acainfo.course.application.port.in;

import com.acainfo.course.application.dto.UpdateCourseCommand;
import com.acainfo.course.domain.model.Course;

/**
 * Use case for updating groups.
 * Input port defining the contract for group updates.
 */
public interface UpdateCourseUseCase {

    /**
     * Update an existing group.
     *
     * @param id Group ID
     * @param command Update data
     * @return The updated group
     */
    Course update(Long id, UpdateCourseCommand command);
}
