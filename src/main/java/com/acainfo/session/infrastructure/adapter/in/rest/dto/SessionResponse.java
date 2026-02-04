package com.acainfo.session.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * REST DTO for session response.
 * Response body for GET /api/sessions
 *
 * Enriched with related entity data to reduce frontend API calls.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionResponse {

    private Long id;
    private Long subjectId;
    private Long groupId;
    private Long scheduleId;
    private Classroom classroom;

    // Enriched data from related entities
    private String subjectName;
    private String subjectCode;
    private String groupName;
    private String groupType;
    private String teacherName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private SessionStatus status;
    private SessionType type;
    private SessionMode mode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate postponedToDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed properties
    private Long durationMinutes;

    // Convenience flags
    private Boolean isScheduled;
    private Boolean isInProgress;
    private Boolean isCompleted;
    private Boolean isCancelled;
    private Boolean isPostponed;
    private Boolean isRegular;
    private Boolean isExtra;
    private Boolean isSchedulingType;
    private Boolean hasGroup;
    private Boolean hasSchedule;
}
