package com.acainfo.group.infrastructure.adapter.in.rest.dto;

import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.GroupType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * REST DTO for group response.
 * Response body for GET /api/groups
 *
 * Enriched with related entity data to reduce frontend API calls.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GroupResponse {

    private Long id;
    private Long subjectId;
    private Long teacherId;
    private GroupType type;
    private GroupStatus status;
    private Integer currentEnrollmentCount;
    private Integer capacity;
    private Integer availableSeats;
    private Integer maxCapacity;
    private BigDecimal pricePerHour;

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
    private Boolean isIntensive;
    private Boolean isRegular;
}
