package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.domain.model.GroupType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * REST DTO for group request response.
 * Response body for GET /api/group-requests
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequestResponse {

    private Long id;
    private Long subjectId;
    private String subjectName;
    private String subjectDegree;
    private Long requesterId;
    private String requesterName;
    private GroupType requestedGroupType;
    private GroupRequestStatus status;
    private Set<Long> supporterIds;
    private Map<Long, String> supporterNames;
    private String justification;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    private Long createdGroupId;
    private String adminResponse;
    private Long processedByAdminId;
    private String processedByAdminName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Convenience properties from domain
    private Integer supporterCount;
    private Boolean hasMinimumSupporters;
    private Integer supportersNeeded;
    private Boolean isPending;
    private Boolean isApproved;
    private Boolean isRejected;
    private Boolean isExpired;
    private Boolean isProcessed;
}
