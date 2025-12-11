package com.acainfo.session.application.dto;

import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;

import java.time.LocalDate;

/**
 * DTO for Session dynamic filtering (Criteria Builder).
 * Used by repository ports to build dynamic queries.
 */
public record SessionFilters(
        Long subjectId,
        Long groupId,
        Long scheduleId,
        SessionType type,
        SessionStatus status,
        SessionMode mode,
        LocalDate dateFrom,
        LocalDate dateTo,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "date";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    /**
     * Compact constructor with default values for pagination.
     */
    public SessionFilters {
        page = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        size = (size != null && size > 0) ? size : DEFAULT_SIZE;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : DEFAULT_SORT_DIRECTION;
    }

    /**
     * Factory method for filtering sessions by group.
     */
    public static SessionFilters byGroup(Long groupId) {
        return new SessionFilters(
                null, groupId, null, null, null, null,
                null, null, null, null, null, null
        );
    }

    /**
     * Factory method for filtering sessions by subject.
     */
    public static SessionFilters bySubject(Long subjectId) {
        return new SessionFilters(
                subjectId, null, null, null, null, null,
                null, null, null, null, null, null
        );
    }

    /**
     * Factory method for filtering sessions by date range.
     */
    public static SessionFilters byDateRange(LocalDate from, LocalDate to) {
        return new SessionFilters(
                null, null, null, null, null, null,
                from, to, null, null, null, null
        );
    }

    /**
     * Factory method for filtering upcoming scheduled sessions for a group.
     */
    public static SessionFilters upcomingForGroup(Long groupId, LocalDate fromDate) {
        return new SessionFilters(
                null, groupId, null, null, SessionStatus.SCHEDULED, null,
                fromDate, null, null, null, "date", "ASC"
        );
    }
}
