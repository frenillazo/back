package com.acainfo.group.application.dto;

import com.acainfo.group.domain.model.GroupStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for updating a group. All fields are optional (null = no change).
 */
public record UpdateGroupCommand(
        Integer capacity,
        GroupStatus status,
        BigDecimal pricePerHour,
        LocalDate startDate,
        LocalDate endDate
) {
}
