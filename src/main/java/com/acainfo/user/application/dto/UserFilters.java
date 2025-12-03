package com.acainfo.user.application.dto;

import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.UserStatus;

/**
 * DTO for User dynamic filtering (Criteria Builder).
 * Used by repository ports to build dynamic queries.
 */
public record UserFilters(
        String email,
        String searchTerm,  // Search in email, firstName, lastName
        UserStatus status,
        RoleType roleType,
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
     * Constructor with default values for pagination.
     */
    public UserFilters {
        page = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        size = (size != null && size > 0) ? size : DEFAULT_SIZE;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : DEFAULT_SORT_DIRECTION;
    }
}
