package com.acainfo.subject.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to archive a subject that has active groups.
 */
public class SubjectHasActiveGroupsException extends BusinessRuleException {

    public SubjectHasActiveGroupsException(Long subjectId, long activeGroupsCount) {
        super(String.format(
                "No se puede archivar la asignatura (ID: %d). Tiene %d grupo(s) activo(s). " +
                "Por favor, cancele o cierre los grupos primero.",
                subjectId, activeGroupsCount
        ));
    }

    @Override
    public String getErrorCode() {
        return "SUBJECT_HAS_ACTIVE_GROUPS";
    }
}
