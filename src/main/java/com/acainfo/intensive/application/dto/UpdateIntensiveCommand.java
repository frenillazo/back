package com.acainfo.intensive.application.dto;

import com.acainfo.intensive.domain.model.IntensiveStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for updating an intensive course.
 * All fields are optional (null = no change).
 */
public record UpdateIntensiveCommand(
        Integer capacity,
        IntensiveStatus status,
        BigDecimal pricePerHour,
        LocalDate startDate,
        LocalDate endDate
) {
}
