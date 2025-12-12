package com.acainfo.enrollment.application.dto;

import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.domain.model.GroupType;

/**
 * DTO for GroupRequest dynamic filtering (Criteria Builder).
 * Used by repository ports to build dynamic queries.
 */
public record GroupRequestFilters(
        Long subjectId,
        Long requesterId,
        GroupType requestedGroupType,
        GroupRequestStatus status,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    /**
     * Compact constructor with default values for pagination.
     */
    public GroupRequestFilters {
        page = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        size = (size != null && size > 0) ? size : DEFAULT_SIZE;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : DEFAULT_SORT_DIRECTION;
    }
}
