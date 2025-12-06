package com.acainfo.schedule.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * REST DTO for updating an existing schedule.
 * Request body for PUT /api/schedules/{id}
 * All fields are optional (null = no change).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateScheduleRequest {

    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Classroom classroom;
}
