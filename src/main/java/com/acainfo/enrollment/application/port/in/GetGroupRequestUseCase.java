package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.domain.model.GroupRequest;
import org.springframework.data.domain.Page;

import java.util.Set;

/**
 * Use case for retrieving group requests.
 * Input port defining the contract for group request queries.
 */
public interface GetGroupRequestUseCase {

    /**
     * Get a group request by ID.
     *
     * @param id Group request ID
     * @return The group request
     * @throws com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException if not found
     */
    GroupRequest getById(Long id);

    /**
     * Find group requests with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of group requests matching the filters
     */
    Page<GroupRequest> findWithFilters(GroupRequestFilters filters);

    /**
     * Get all supporters for a group request.
     *
     * @param groupRequestId Group request ID
     * @return Set of supporter student IDs
     * @throws com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException if not found
     */
    Set<Long> getSupporters(Long groupRequestId);
}
