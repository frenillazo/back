package com.acainfo.reservation.infrastructure.adapter.in.rest.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

/**
 * REST DTO for recording attendance for multiple reservations.
 * Request body for POST /api/sessions/{sessionId}/attendance
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkRecordAttendanceRequest {

    @NotNull(message = "Attendance map is required")
    @NotEmpty(message = "Attendance map cannot be empty")
    private Map<Long, AttendanceStatus> attendanceMap;
}
