package com.acainfo.course.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for creating a course.
 *
 * @param subjectId      Subject the course belongs to (required)
 * @param teacherId      Teacher in charge (optional, null = not assigned yet)
 * @param startDate      Inclusive — first day sessions can be generated (required)
 * @param endDate        Inclusive — last day sessions can be generated (required)
 * @param capacity       Physical seats (null = unlimited, virtual/dual course)
 * @param pricePerMonth  Informative price per month (optional)
 */
public record CreateCourseCommand(
        Long subjectId,
        Long teacherId,
        LocalDate startDate,
        LocalDate endDate,
        Integer capacity,
        BigDecimal pricePerMonth
) {
}
