package com.acainfo.course.application.port.in;

import com.acainfo.course.application.dto.CourseFilters;
import com.acainfo.course.domain.model.Course;
import org.springframework.data.domain.Page;

/**
 * Use case for retrieving groups.
 * Input port defining the contract for group queries.
 */
public interface GetCourseUseCase {

    /**
     * Get a group by ID.
     *
     * @param id Group ID
     * @return The group
     * @throws com.acainfo.course.domain.exception.CourseNotFoundException if not found
     */
    Course getById(Long id);

    /**
     * Find groups with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of groups matching the filters
     */
    Page<Course> findWithFilters(CourseFilters filters);
}
