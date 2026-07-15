package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when creating or renaming a folder to a name
 * that already exists within the same subject.
 */
public class DuplicateFolderNameException extends BusinessRuleException {

    public DuplicateFolderNameException(String name) {
        super("Ya existe una carpeta llamada '" + name + "' en esta asignatura");
    }

    @Override
    public String getErrorCode() {
        return "DUPLICATE_FOLDER_NAME";
    }
}
