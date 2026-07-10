package com.acainfo.course.application.port.in;

import com.acainfo.course.application.dto.CreateCourseCommand;
import com.acainfo.course.domain.model.Course;

/**
 * Use case for creating groups.
 * Input port defining the contract for group creation.
 */
public interface CreateCourseUseCase {

    /**
     * Create a new group.
     *
     * @param command Group creation data
     * @return The created group
     */
    Course create(CreateCourseCommand command);
}
