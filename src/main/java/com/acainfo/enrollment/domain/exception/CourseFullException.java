package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when trying to enroll in a full course without waiting list option.
 */
public class CourseFullException extends BusinessRuleException {

    public CourseFullException(Long courseId) {
        super("Course " + courseId + " is full and has no available seats");
    }

    @Override
    public String getErrorCode() {
        return "COURSE_FULL";
    }
}
