package acainfo.back.attendance.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO for registering individual attendance.
 */
@Builder
public record RegisterAttendanceRequest(
    @NotNull(message = "Student ID is required")
    Long studentId,

    @NotBlank(message = "Attendance status is required")
    @Pattern(
        regexp = "PRESENTE|AUSENTE|TARDANZA",
        message = "Status must be one of: PRESENTE, AUSENTE, TARDANZA"
    )
    String status,

    @Min(value = 1, message = "Minutes late must be at least 1")
    @Max(value = 300, message = "Minutes late cannot exceed 300 (5 hours)")
    Integer minutesLate,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    String notes
) {
}
