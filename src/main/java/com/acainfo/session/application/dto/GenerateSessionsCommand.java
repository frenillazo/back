package com.acainfo.session.application.dto;

import java.time.LocalDate;

/**
 * Command DTO for generating sessions from schedules.
 * Used to bulk-generate REGULAR sessions for a group or all groups.
 */
public record GenerateSessionsCommand(
        Long groupId,          // Optional: null = generate for all groups with schedules
        LocalDate startDate,   // Start of the period to generate sessions
        LocalDate endDate      // End of the period to generate sessions
) {
    /**
     * Generate sessions for a specific group.
     */
    public static GenerateSessionsCommand forGroup(Long groupId, LocalDate startDate, LocalDate endDate) {
        return new GenerateSessionsCommand(groupId, startDate, endDate);
    }

    /**
     * Generate sessions for all groups with schedules.
     */
    public static GenerateSessionsCommand forAllGroups(LocalDate startDate, LocalDate endDate) {
        return new GenerateSessionsCommand(null, startDate, endDate);
    }
}
