package com.acainfo.course.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when trying to enroll in a course that has reached its maximum capacity.
 */
public class CourseCapacityExceededException extends BusinessRuleException {

    public CourseCapacityExceededException(Long courseId, int maxCapacity) {
        super(String.format("Course %d has reached its maximum capacity of %d students", courseId, maxCapacity));
    }

    public CourseCapacityExceededException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "COURSE_CAPACITY_EXCEEDED";
    }
}
