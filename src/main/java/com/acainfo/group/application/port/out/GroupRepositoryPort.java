package com.acainfo.group.application.port.out;

import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.domain.model.SubjectGroup;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Output port for SubjectGroup persistence.
 * Defines the contract for SubjectGroup repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface GroupRepositoryPort {

    /**
     * Save or update a group.
     *
     * @param group Domain group to persist
     * @return Persisted group with ID
     */
    SubjectGroup save(SubjectGroup group);

    /**
     * Find group by ID.
     *
     * @param id Group ID
     * @return Optional containing the group if found
     */
    Optional<SubjectGroup> findById(Long id);

    /**
     * Find groups with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of groups matching filters
     */
    Page<SubjectGroup> findWithFilters(GroupFilters filters);

    /**
     * Delete a group by ID.
     *
     * @param id Group ID
     */
    void delete(Long id);

    /**
     * Find all groups.
     *
     * @return List of all groups
     */
    List<SubjectGroup> findAll();
}
