package com.acainfo.subject.application.port.in;

import com.acainfo.subject.application.dto.UpdateSubjectCommand;
import com.acainfo.subject.domain.model.Subject;

/**
 * Use case for updating subjects.
 * Input port defining the contract for subject updates.
 */
public interface UpdateSubjectUseCase {

    /**
     * Update an existing subject.
     *
     * @param id Subject ID
     * @param command Update data
     * @return Updated subject
     */
    Subject update(Long id, UpdateSubjectCommand command);
}
