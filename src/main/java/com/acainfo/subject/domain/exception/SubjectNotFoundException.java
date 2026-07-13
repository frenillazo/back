package com.acainfo.subject.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a subject is not found.
 */
public class SubjectNotFoundException extends NotFoundException {

    public SubjectNotFoundException(Long subjectId) {
        super("Asignatura no encontrada con id: " + subjectId);
    }

    public SubjectNotFoundException(String code) {
        super("Asignatura no encontrada con código: " + code);
    }
}
