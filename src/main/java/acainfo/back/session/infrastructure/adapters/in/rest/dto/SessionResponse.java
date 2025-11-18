package acainfo.back.session.infrastructure.adapters.in.rest.dto;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.model.Session;
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
    /**
     * Converts a Session entity to a SessionResponse DTO.
     */
    public static SessionResponse fromEntity(Session session) {
        return SessionResponse.builder()
            .id(session.getId())
            .subjectGroupId(session.getSubjectGroup().getId())
            .subjectGroupName(session.getSubjectGroup().getDisplayName())
            .subjectCode(session.getSubjectGroup().getSubject() != null ?
                session.getSubjectGroup().getSubject().getCode() : null)
            .subjectName(session.getSubjectGroup().getSubject() != null ?
                session.getSubjectGroup().getSubject().getName() : null)
            .teacherId(session.getSubjectGroup().getTeacher() != null ?
                session.getSubjectGroup().getTeacher().getId() : null)
            .teacherName(session.getSubjectGroup().getTeacher() != null ?
                session.getSubjectGroup().getTeacher().getFirstName() + " " +
                session.getSubjectGroup().getTeacher().getLastName() : null)
            .generatedFromScheduleId(session.getGeneratedFromSchedule() != null ?
                session.getGeneratedFromSchedule().getId() : null)
            .type(session.getType())
            .scheduledStart(session.getScheduledStart())
            .scheduledEnd(session.getScheduledEnd())
            .actualStart(session.getActualStart())
            .actualEnd(session.getActualEnd())
            .mode(session.getMode())
            .status(session.getStatus())
            .classroom(session.getClassroom())
            .zoomMeetingId(session.getZoomMeetingId())
            .cancellationReason(session.getCancellationReason())
            .postponementReason(session.getPostponementReason())
            .originalSessionId(session.getOriginalSessionId())
            .recoveryForSessionId(session.getRecoveryForSessionId())
            .notes(session.getNotes())
            .topicsCovered(session.getTopicsCovered())
            .scheduledDurationMinutes(session.getScheduledDurationInMinutes())
            .actualDurationMinutes(session.getActualDurationInMinutes())
            .createdAt(session.getCreatedAt())
            .updatedAt(session.getUpdatedAt())
            .build();
    }
}
