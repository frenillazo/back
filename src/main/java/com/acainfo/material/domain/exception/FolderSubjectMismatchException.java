package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when assigning a material to a folder
 * that belongs to a different subject.
 */
public class FolderSubjectMismatchException extends BusinessRuleException {

    public FolderSubjectMismatchException(Long folderId, Long subjectId) {
        super("La carpeta " + folderId + " no pertenece a la asignatura " + subjectId);
    }

    @Override
    public String getErrorCode() {
        return "FOLDER_SUBJECT_MISMATCH";
    }
}
