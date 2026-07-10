package com.acainfo.course.domain.exception;

import com.acainfo.shared.domain.exception.ValidationException;

/**
 * Exception thrown when course data is invalid.
 */
public class InvalidCourseDataException extends ValidationException {

    public InvalidCourseDataException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "COURSE_INVALID_DATA";
    }
}
