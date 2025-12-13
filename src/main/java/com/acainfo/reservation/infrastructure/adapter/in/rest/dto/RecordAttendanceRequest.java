package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for recording attendance for a single reservation.
 * Request body for PUT /api/reservations/{id}/attendance
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordAttendanceRequest {

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
}
