package com.acainfo.course.infrastructure.adapter.in.rest.dto;

import com.acainfo.course.domain.model.CourseStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REST DTO for updating an existing course.
 * Request body for PUT /api/courses/{id}. All fields optional.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateCourseRequest {

    private CourseStatus status;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @DecimalMin(value = "0.01", message = "pricePerMonth must be > 0")
    private BigDecimal pricePerMonth;

    private Long teacherId;  // optional: reassign teacher

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
