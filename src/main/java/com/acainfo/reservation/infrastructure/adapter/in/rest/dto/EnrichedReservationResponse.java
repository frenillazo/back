package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Extended reservation response with session details.
 * Avoids N+1 frontend queries on the student reservations views.
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
    private Boolean canBeCancelled;

    // === Session enrichment fields ===
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sessionStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sessionEndTime;

    private String sessionStatus;
    private String classroom;

    // Subject & course details
    private String subjectName;
    private String subjectCode;
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
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .isConfirmed(r.getIsConfirmed())
                .isCancelled(r.getIsCancelled())
                .isInPerson(r.getIsInPerson())
                .isOnline(r.getIsOnline())
                .canBeCancelled(r.getCanBeCancelled())
                .build();
    }
}
