package com.acainfo.group.application.port.in;

import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.domain.model.SubjectGroup;
import org.springframework.data.domain.Page;

/**
 * Use case for retrieving groups.
 * Input port defining the contract for group queries.
 */
public interface GetGroupUseCase {

    /**
     * Get a group by ID.
     *
     * @param id Group ID
     * @return The group
     * @throws com.acainfo.group.domain.exception.GroupNotFoundException if not found
     */
    SubjectGroup getById(Long id);

    /**
     * Find groups with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of groups matching the filters
     */
    Page<SubjectGroup> findWithFilters(GroupFilters filters);
}
