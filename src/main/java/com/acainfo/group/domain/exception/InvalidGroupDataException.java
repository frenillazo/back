package com.acainfo.group.domain.exception;

import com.acainfo.shared.domain.exception.ValidationException;

/**
 * Exception thrown when group data is invalid.
 */
public class InvalidGroupDataException extends ValidationException {

    public InvalidGroupDataException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "GROUP_INVALID_DATA";
    }
}
