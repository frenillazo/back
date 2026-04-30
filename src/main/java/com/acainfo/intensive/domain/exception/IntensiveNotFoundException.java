package com.acainfo.intensive.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when an intensive course is not found.
 */
public class IntensiveNotFoundException extends NotFoundException {

    public IntensiveNotFoundException(Long id) {
        super("Intensive not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "INTENSIVE_NOT_FOUND";
    }
}
