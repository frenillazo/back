package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

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
    private String studentName;
    private String studentEmail;
    private Long sessionId;
    private Long enrollmentId;

    // Reservation fields
    private ReservationMode mode;
    private ReservationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

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
    private Boolean canBeCancelled;
}
