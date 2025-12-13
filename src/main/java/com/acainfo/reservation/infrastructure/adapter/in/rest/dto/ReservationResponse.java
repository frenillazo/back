package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * REST DTO for reservation response.
 * Response body for GET /api/reservations
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;
    private Long studentId;
    private Long sessionId;
    private Long enrollmentId;

    // Reservation fields
    private ReservationMode mode;
    private ReservationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

    // Online request fields
    private OnlineRequestStatus onlineRequestStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime onlineRequestedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime onlineRequestProcessedAt;

    private Long onlineRequestProcessedById;

    // Attendance fields
    private AttendanceStatus attendanceStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime attendanceRecordedAt;

    private Long attendanceRecordedById;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Convenience flags from domain
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
}
