package com.acainfo.schedule.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * REST DTO for schedule response.
 * Response body for GET /api/schedules
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ScheduleResponse {

    private Long id;
    private Long groupId;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Classroom classroom;
    private String classroomDisplayName;

    private Long durationMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
