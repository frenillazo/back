package com.acainfo.payment.application.dto;

import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;

/**
 * DTO for Payment dynamic filtering (Criteria Builder).
 * Used by repository ports to build dynamic queries.
 */
public record PaymentFilters(
        Long studentId,
        String studentEmail,
        Long enrollmentId,
        PaymentStatus status,
        PaymentType type,
        Integer billingMonth,
        Integer billingYear,
        Boolean isOverdue,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "generatedAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    /**
     * Compact constructor with default values for pagination.
     */
    public PaymentFilters {
        page = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        size = (size != null && size > 0) ? size : DEFAULT_SIZE;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : DEFAULT_SORT_DIRECTION;
    }
}
