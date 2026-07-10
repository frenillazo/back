package com.acainfo.course.application.dto;

import com.acainfo.course.domain.model.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for updating a course. All fields are optional (null = no change).
 */
public record UpdateCourseCommand(
        Integer capacity,
        CourseStatus status,
        BigDecimal pricePerMonth,
        Long teacherId,
        LocalDate startDate,
        LocalDate endDate
) {
}
