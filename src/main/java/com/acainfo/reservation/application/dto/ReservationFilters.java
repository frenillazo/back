package com.acainfo.reservation.application.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;

/**
 * DTO for SessionReservation dynamic filtering (Criteria Builder).
 * Used by repository ports to build dynamic queries.
 */
public record ReservationFilters(
        Long studentId,
        Long sessionId,
        Long enrollmentId,
        ReservationStatus status,
        ReservationMode mode,
        OnlineRequestStatus onlineRequestStatus,
        AttendanceStatus attendanceStatus,
        Boolean hasAttendanceRecorded,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "reservedAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    /**
     * Compact constructor with default values for pagination.
     */
    public ReservationFilters {
        page = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        size = (size != null && size > 0) ? size : DEFAULT_SIZE;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : DEFAULT_SORT_DIRECTION;
    }
}
