package com.acainfo.subject.application.port.out;

import com.acainfo.subject.application.dto.SubjectFilters;
import com.acainfo.subject.domain.model.Subject;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Subject persistence.
 * Defines the contract for Subject repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface SubjectRepositoryPort {

    /**
     * Save or update a subject.
     *
     * @param subject Domain subject to persist
     * @return Persisted subject with ID
     */
    Subject save(Subject subject);

    /**
     * Find subject by ID.
     *
     * @param id Subject ID
     * @return Optional containing the subject if found
     */
    Optional<Subject> findById(Long id);

    /**
     * Find subject by code (case insensitive).
     *
     * @param code Subject code
     * @return Optional containing the subject if found
     */
    Optional<Subject> findByCode(String code);

    /**
     * Check if subject code already exists (case insensitive).
     *
     * @param code Subject code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Find subjects with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of subjects matching filters
     */
    Page<Subject> findWithFilters(SubjectFilters filters);

    /**
     * Delete a subject by ID.
     *
     * @param id Subject ID
     */
    void delete(Long id);

    /**
     * Find subjects by a list of IDs.
     *
     * @param ids List of subject IDs
     * @return List of subjects found
     */
    List<Subject> findByIds(List<Long> ids);
}
