package com.acainfo.schedule.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * REST DTO for creating a new schedule.
 * Request body for POST /api/schedules
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateScheduleRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "Classroom is required")
    private Classroom classroom;
}
