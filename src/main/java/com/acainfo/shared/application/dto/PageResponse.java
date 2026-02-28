package com.acainfo.shared.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 * Unified across all modules — field names match what the frontend expects.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
    /**
     * Create from Spring Data Page.
     */
    public static <T> PageResponse<T> of(Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isFirst(),
                springPage.isLast(),
                springPage.isEmpty()
        );
    }

    /**
     * Create from raw values (for manual construction).
     */
    public static <T> PageResponse<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page >= totalPages - 1,
                content.isEmpty()
        );
    }
}
