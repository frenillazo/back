package com.acainfo.course.infrastructure.adapter.in.rest.dto;

import com.acainfo.course.domain.model.CourseStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST DTO for course response. Enriched with related entity data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CourseResponse {

    private Long id;
    private String name;
    private Long subjectId;
    private Long teacherId;
    private CourseStatus status;
    private Integer currentEnrollmentCount;   // computed dynamically (single source of truth)
    private Integer capacity;                  // null = unlimited (virtual/dual)
    private Integer availableSeats;            // null = unlimited
    private BigDecimal pricePerMonth;          // informative only

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // Enriched data from related entities
    private String subjectName;
    private String subjectCode;
    private String teacherName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Convenience flags
    private Boolean isOpen;
    private Boolean canEnroll;

    // Schedule summary for display
    private List<ScheduleSummary> schedules;
}
