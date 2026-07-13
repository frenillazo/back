package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an uploaded file type is not in the whitelist.
 */
public class InvalidFileTypeException extends BusinessRuleException {

    public InvalidFileTypeException(String extension) {
        super("Tipo de archivo no permitido: " + extension);
    }

    public InvalidFileTypeException(String extension, String allowedTypes) {
        super("Tipo de archivo '" + extension + "' no permitido. Tipos permitidos: " + allowedTypes);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_FILE_TYPE";
    }
}
