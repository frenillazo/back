package acainfo.back.attendance.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO for updating attendance status.
 */
@Builder
public record UpdateAttendanceRequest(
    @NotBlank(message = "New status is required")
    @Pattern(
        regexp = "PRESENTE|AUSENTE|TARDANZA",
        message = "Status must be one of: PRESENTE, AUSENTE, TARDANZA"
    )
    String newStatus,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    String notes
) {
}
