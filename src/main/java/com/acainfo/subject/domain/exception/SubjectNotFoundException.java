package com.acainfo.subject.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a subject is not found.
 */
public class SubjectNotFoundException extends NotFoundException {

    public SubjectNotFoundException(Long subjectId) {
        super("Subject not found with id: " + subjectId);
    }

    public SubjectNotFoundException(String code) {
        super("Subject not found with code: " + code);
    }
}
