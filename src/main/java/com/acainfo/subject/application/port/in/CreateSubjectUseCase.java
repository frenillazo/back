package com.acainfo.subject.application.port.in;

import com.acainfo.subject.application.dto.CreateSubjectCommand;
import com.acainfo.subject.domain.model.Subject;

/**
 * Use case for creating subjects.
 * Input port defining the contract for subject creation.
 */
public interface CreateSubjectUseCase {

    /**
     * Create a new subject.
     *
     * @param command Subject creation data
     * @return Created subject
     */
    Subject create(CreateSubjectCommand command);
}
