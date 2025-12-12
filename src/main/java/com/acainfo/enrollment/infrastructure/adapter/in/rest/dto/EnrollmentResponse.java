package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * REST DTO for enrollment response.
 * Response body for GET /api/enrollments
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private Long groupId;
    private EnrollmentStatus status;
    private Integer waitingListPosition;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime enrolledAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime promotedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime withdrawnAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Convenience flags from domain
    private Boolean isActive;
    private Boolean isOnWaitingList;
    private Boolean isWithdrawn;
    private Boolean isCompleted;
    private Boolean wasPromotedFromWaitingList;
    private Boolean canBeWithdrawn;
}
