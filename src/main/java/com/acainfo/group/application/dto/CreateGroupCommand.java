package com.acainfo.group.application.dto;

import com.acainfo.group.domain.model.GroupType;

import java.math.BigDecimal;

/**
 * Command DTO for creating a group.
 * Encapsulates the data required to create a new subject group.
 */
public record CreateGroupCommand(
        Long subjectId,
        Long teacherId,
        GroupType type,
        Integer capacity,       // Optional: null = use default based on type (24 or 50)
        BigDecimal pricePerHour // Optional: null = use default price (15€/hour)
) {
}
