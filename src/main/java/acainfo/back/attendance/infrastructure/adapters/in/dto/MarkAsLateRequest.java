package acainfo.back.attendance.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO for marking attendance as late.
 */
@Builder
public record MarkAsLateRequest(
    @NotNull(message = "Minutes late is required")
    @Min(value = 1, message = "Minutes late must be at least 1")
    @Max(value = 300, message = "Minutes late cannot exceed 300 (5 hours)")
    Integer minutesLate,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    String notes
) {
}
