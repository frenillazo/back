package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an uploaded file type is not in the whitelist.
 */
public class InvalidFileTypeException extends BusinessRuleException {

    public InvalidFileTypeException(String extension) {
        super("File type not allowed: " + extension);
    }

    public InvalidFileTypeException(String extension, String allowedTypes) {
        super("File type '" + extension + "' not allowed. Allowed types: " + allowedTypes);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_FILE_TYPE";
    }
}
