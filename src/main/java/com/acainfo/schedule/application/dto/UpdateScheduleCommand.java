package com.acainfo.schedule.application.dto;

import com.acainfo.schedule.domain.model.Classroom;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Command DTO for updating a schedule.
 * All fields are optional (null = no change).
 */
public record UpdateScheduleCommand(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Classroom classroom
) {
}
