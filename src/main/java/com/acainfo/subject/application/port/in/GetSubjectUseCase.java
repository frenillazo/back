package com.acainfo.subject.application.port.in;

import com.acainfo.subject.application.dto.SubjectFilters;
import com.acainfo.subject.domain.model.Subject;
import org.springframework.data.domain.Page;

/**
 * Use case for retrieving subjects.
 * Input port defining the contract for subject queries.
 */
public interface GetSubjectUseCase {

    /**
     * Get subject by ID.
     *
     * @param id Subject ID
     * @return Subject
     */
    Subject getById(Long id);

    /**
     * Get subject by code.
     *
     * @param code Subject code
     * @return Subject
     */
    Subject getByCode(String code);

    /**
     * Find subjects with filters (paginated).
     *
     * @param filters Filter criteria
     * @return Page of subjects
     */
    Page<Subject> findWithFilters(SubjectFilters filters);
}
