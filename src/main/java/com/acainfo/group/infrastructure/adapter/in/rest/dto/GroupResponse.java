package com.acainfo.group.infrastructure.adapter.in.rest.dto;

import com.acainfo.group.domain.model.GroupStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST DTO for group response. Enriched with related entity data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GroupResponse {

    private Long id;
    private String name;
    private Long subjectId;
    private Long teacherId;
    private GroupStatus status;
    private Integer currentEnrollmentCount;
    private Integer capacity;
    private Integer availableSeats;
    private Integer maxCapacity;
    private BigDecimal pricePerHour;

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
