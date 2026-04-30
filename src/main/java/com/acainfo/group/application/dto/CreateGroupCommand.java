package com.acainfo.group.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for creating a regular group.
 *
 * @param subjectId      Subject the group belongs to (required)
 * @param teacherId      Teacher in charge (required)
 * @param startDate      Inclusive — first day sessions can be generated (required)
 * @param endDate        Inclusive — last day sessions can be generated (required)
 * @param capacity       Optional custom capacity (null = use default 24)
 * @param pricePerHour   Optional custom price (null = use default 15€/h)
 */
public record CreateGroupCommand(
        Long subjectId,
        Long teacherId,
        LocalDate startDate,
        LocalDate endDate,
        Integer capacity,
        BigDecimal pricePerHour
) {
}
