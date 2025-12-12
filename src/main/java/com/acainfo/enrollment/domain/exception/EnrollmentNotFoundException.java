package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when an enrollment is not found.
 */
public class EnrollmentNotFoundException extends NotFoundException {

    public EnrollmentNotFoundException(Long id) {
        super("Enrollment not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "ENROLLMENT_NOT_FOUND";
    }
}
