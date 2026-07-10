package com.acainfo.schedule.infrastructure.adapter.in.rest.dto;

import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.schedule.domain.model.Classroom;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * REST DTO for enriched schedule response.
 * Includes group, subject, and teacher information for global schedule views.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ScheduleEnrichedResponse {

    // Schedule fields
    private Long id;
    private Long courseId;
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

    // Enriched data from Group
    private CourseStatus courseStatus;
    private BigDecimal pricePerMonth;

    // Enriched data from Subject
    private Long subjectId;
    private String subjectName;
    private String subjectCode;

    // Enriched data from Teacher
    private Long teacherId;
    private String teacherName;
}
