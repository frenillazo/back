package acainfo.back.attendance.infrastructure.adapters.in.dto;

import acainfo.back.attendance.domain.model.Attendance;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Attendance responses.
 * Contains all attendance information for client consumption.
 */
@Builder
public record AttendanceResponse(
    Long id,
    Long sessionId,
    LocalDateTime sessionScheduledStart,
    String sessionSubjectGroupName,
    Long studentId,
    AttendanceStatus status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime recordedAt,
    Long recordedById,
    String notes,
    Integer minutesLate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime justifiedAt,
    Long justifiedById,
    boolean countsAsEffectiveAttendance,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
    /**
     * Converts an Attendance entity to an AttendanceResponse DTO.
     */
    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
            .id(attendance.getId())
            .sessionId(attendance.getSession().getId())
            .sessionScheduledStart(attendance.getSession().getScheduledStart())
            .sessionSubjectGroupName(attendance.getSession().getSubjectGroup().getDisplayName())
            .studentId(attendance.getStudentId())
            .status(attendance.getStatus())
            .recordedAt(attendance.getRecordedAt())
            .recordedById(attendance.getRecordedById())
            .notes(attendance.getNotes())
            .minutesLate(attendance.getMinutesLate())
            .justifiedAt(attendance.getJustifiedAt())
            .justifiedById(attendance.getJustifiedById())
            .countsAsEffectiveAttendance(attendance.countsAsEffectiveAttendance())
            .createdAt(attendance.getCreatedAt())
            .updatedAt(attendance.getUpdatedAt())
            .build();
    }
}
