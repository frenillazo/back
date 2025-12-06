package com.acainfo.schedule.application.dto;

import com.acainfo.schedule.domain.model.Classroom;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Command DTO for creating a schedule.
 * Encapsulates the data required to create a new schedule.
 */
public record CreateScheduleCommand(
        Long groupId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Classroom classroom
) {
}
