package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when file storage operations fail.
 */
public class FileStorageException extends BusinessRuleException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "FILE_STORAGE_ERROR";
    }
}
