package com.acainfo.intensive.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for creating an intensive course.
 *
 * @param subjectId      Subject the intensive belongs to (required)
 * @param teacherId      Teacher in charge (required)
 * @param startDate      First valid date for sessions (inclusive, required)
 * @param endDate        Last valid date for sessions (inclusive, required)
 * @param capacity       Optional custom capacity (null = use default 50)
 * @param pricePerHour   Optional custom price (null = use default 15€/h)
 */
public record CreateIntensiveCommand(
        Long subjectId,
        Long teacherId,
        LocalDate startDate,
        LocalDate endDate,
        Integer capacity,
        BigDecimal pricePerHour
) {
}
