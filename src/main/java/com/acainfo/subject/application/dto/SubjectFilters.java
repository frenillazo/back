package com.acainfo.subject.application.dto;

import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.SubjectStatus;

/**
 * DTO for Subject dynamic filtering (Criteria Builder).
 * Used by repository ports to build dynamic queries.
 */
public record SubjectFilters(
        String code,
        String searchTerm,  // Search in code, name
        Degree degree,
        SubjectStatus status,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "name";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    /**
     * Compact constructor with default values for pagination.
     */
    public SubjectFilters {
        page = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        size = (size != null && size > 0) ? size : DEFAULT_SIZE;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : DEFAULT_SORT_DIRECTION;
    }
}
