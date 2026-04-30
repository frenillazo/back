package com.acainfo.intensive.infrastructure.adapter.in.rest.dto;

import com.acainfo.intensive.domain.model.IntensiveStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * REST response for an intensive course, enriched with subject + teacher names.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IntensiveResponse {

    private Long id;
    private String name;
    private Long subjectId;
    private Long teacherId;
    private IntensiveStatus status;

    private Integer currentEnrollmentCount;
    private Integer capacity;
    private Integer maxCapacity;
    private Integer availableSeats;

    private BigDecimal pricePerHour;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // Enriched data
    private String subjectName;
    private String subjectCode;
    private String teacherName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Boolean isOpen;
    private Boolean canEnroll;
}
