package com.acainfo.course.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REST DTO for creating a new course.
 * Request body for POST /api/courses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateCourseRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private Long teacherId;  // optional: null = not assigned yet

    @NotNull(message = "startDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;  // null = unlimited (virtual/dual course)

    @DecimalMin(value = "0.01", message = "Price per month must be greater than 0")
    private BigDecimal pricePerMonth;  // informative only
}
