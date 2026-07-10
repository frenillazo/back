package com.acainfo.course.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a course is not found.
 */
public class CourseNotFoundException extends NotFoundException {

    public CourseNotFoundException(Long id) {
        super("Course not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "COURSE_NOT_FOUND";
    }
}
