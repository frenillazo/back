package acainfo.back.attendance.infrastructure.adapters.in.dto;

import acainfo.back.attendance.domain.model.Attendance;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Attendance responses.
 * Contains all attendance information for client consumption.
 * Updated to include enrollment and user information.
 */
@Builder
public record AttendanceResponse(
    Long id,
    Long sessionId,
    LocalDateTime sessionScheduledStart,
    String sessionSubjectGroupName,
    Long enrollmentId,
    Long studentId,
    String studentName,
    AttendanceStatus status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime recordedAt,
    Long recordedById,
    String recordedByName,
    String notes,
    Integer minutesLate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime justifiedAt,
    Long justifiedById,
    String justifiedByName,
    boolean countsAsEffectiveAttendance,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
    /**
     * Converts an Attendance entity to an AttendanceResponse DTO.
     * Includes enrollment and user information.
     */
    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
            .id(attendance.getId())
            .sessionId(attendance.getSession().getId())
            .sessionScheduledStart(attendance.getSession().getScheduledStart())
            .sessionSubjectGroupName(attendance.getSession().getSubjectGroup().getDisplayName())
            .enrollmentId(attendance.getEnrollment().getId())
            .studentId(attendance.getEnrollment().getStudent().getId())
            .studentName(attendance.getEnrollment().getStudent().getFirstName() + " " +
                        attendance.getEnrollment().getStudent().getLastName())
            .status(attendance.getStatus())
            .recordedAt(attendance.getRecordedAt())
            .recordedById(attendance.getRecordedBy().getId())
            .recordedByName(attendance.getRecordedBy().getFirstName() + " " +
                           attendance.getRecordedBy().getLastName())
            .notes(attendance.getNotes())
            .minutesLate(attendance.getMinutesLate())
            .justifiedAt(attendance.getJustifiedAt())
            .justifiedById(attendance.getJustifiedBy() != null ? attendance.getJustifiedBy().getId() : null)
            .justifiedByName(attendance.getJustifiedBy() != null ?
                            attendance.getJustifiedBy().getFirstName() + " " +
                            attendance.getJustifiedBy().getLastName() : null)
            .countsAsEffectiveAttendance(attendance.countsAsEffectiveAttendance())
            .createdAt(attendance.getCreatedAt())
            .updatedAt(attendance.getUpdatedAt())
            .build();
    }
}
