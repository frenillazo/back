package acainfo.back.session.infrastructure.adapters.in.dto;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Session responses.
 * Contains all session information for client consumption.
 */
@Builder
public record SessionResponse(
    Long id,
    Long subjectGroupId,
    String subjectGroupName,
    String subjectCode,
    String subjectName,
    Long teacherId,
    String teacherName,
    Long generatedFromScheduleId,
    SessionType type,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime scheduledStart,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime scheduledEnd,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime actualStart,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime actualEnd,
    SessionMode mode,
    SessionStatus status,
    Classroom classroom,
    String zoomMeetingId,
    String cancellationReason,
    String postponementReason,
    Long originalSessionId,
    Long recoveryForSessionId,
    String notes,
    String topicsCovered,
    Long scheduledDurationMinutes,
    Long actualDurationMinutes,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
}
