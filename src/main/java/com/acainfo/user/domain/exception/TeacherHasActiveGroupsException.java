package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to delete a teacher who has active groups.
 */
public class TeacherHasActiveGroupsException extends BusinessRuleException {

    public TeacherHasActiveGroupsException(Long teacherId, long activeGroupsCount) {
        super(String.format(
                "No se puede eliminar el profesor (ID: %d). Tiene %d grupo(s) activo(s). " +
                "Por favor, reasigne o cancele los grupos primero.",
                teacherId, activeGroupsCount
        ));
    }

    @Override
    public String getErrorCode() {
        return "TEACHER_HAS_ACTIVE_GROUPS";
    }
}
