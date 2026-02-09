package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Extended reservation response with session details.
 * Used by the student attendance history page to avoid N+1 frontend queries.
 * GET /api/reservations/student/{studentId}/enriched
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedReservationResponse {

    // === Reservation fields (mirrored from ReservationResponse) ===
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long sessionId;
    private Long enrollmentId;

    private ReservationMode mode;
    private ReservationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

    // Online request
    private OnlineRequestStatus onlineRequestStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime onlineRequestedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime onlineRequestProcessedAt;

    private Long onlineRequestProcessedById;

    // Attendance
    private AttendanceStatus attendanceStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime attendanceRecordedAt;

    private Long attendanceRecordedById;

    // Audit
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Convenience flags
    private Boolean isConfirmed;
    private Boolean isCancelled;
    private Boolean isInPerson;
    private Boolean isOnline;
    private Boolean hasOnlineRequest;
    private Boolean isOnlineRequestPending;
    private Boolean isOnlineRequestApproved;
    private Boolean isOnlineRequestRejected;
    private Boolean hasAttendanceRecorded;
    private Boolean wasPresent;
    private Boolean wasAbsent;
    private Boolean canBeCancelled;
    private Boolean canRequestOnline;

    // === Session enrichment fields ===
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sessionStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sessionEndTime;

    private String sessionStatus;
    private String classroom;

    // Subject & group details
    private String subjectName;
    private String subjectCode;
    private String groupType;
    private String teacherName;

    /**
     * Build from a ReservationResponse + session enrichment data.
     */
    public static EnrichedReservationResponse from(ReservationResponse r) {
        return EnrichedReservationResponse.builder()
                .id(r.getId())
                .studentId(r.getStudentId())
                .studentName(r.getStudentName())
                .studentEmail(r.getStudentEmail())
                .sessionId(r.getSessionId())
                .enrollmentId(r.getEnrollmentId())
                .mode(r.getMode())
                .status(r.getStatus())
                .reservedAt(r.getReservedAt())
                .cancelledAt(r.getCancelledAt())
                .onlineRequestStatus(r.getOnlineRequestStatus())
                .onlineRequestedAt(r.getOnlineRequestedAt())
                .onlineRequestProcessedAt(r.getOnlineRequestProcessedAt())
                .onlineRequestProcessedById(r.getOnlineRequestProcessedById())
                .attendanceStatus(r.getAttendanceStatus())
                .attendanceRecordedAt(r.getAttendanceRecordedAt())
                .attendanceRecordedById(r.getAttendanceRecordedById())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .isConfirmed(r.getIsConfirmed())
                .isCancelled(r.getIsCancelled())
                .isInPerson(r.getIsInPerson())
                .isOnline(r.getIsOnline())
                .hasOnlineRequest(r.getHasOnlineRequest())
                .isOnlineRequestPending(r.getIsOnlineRequestPending())
                .isOnlineRequestApproved(r.getIsOnlineRequestApproved())
                .isOnlineRequestRejected(r.getIsOnlineRequestRejected())
                .hasAttendanceRecorded(r.getHasAttendanceRecorded())
                .wasPresent(r.getWasPresent())
                .wasAbsent(r.getWasAbsent())
                .canBeCancelled(r.getCanBeCancelled())
                .canRequestOnline(r.getCanRequestOnline())
                .build();
    }
}
