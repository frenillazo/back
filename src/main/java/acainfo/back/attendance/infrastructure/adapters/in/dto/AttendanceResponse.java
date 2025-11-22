package acainfo.back.attendance.infrastructure.adapters.in.dto;

import acainfo.back.attendance.domain.model.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Attendance responses.
 * Contains all attendance information for client consumption.
 *
 * Note: Conversion from domain to DTO is handled by AttendanceDtoMapper
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
}
