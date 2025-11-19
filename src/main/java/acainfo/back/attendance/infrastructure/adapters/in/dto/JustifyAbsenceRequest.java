package acainfo.back.attendance.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO for justifying an absence.
 */
@Builder
public record JustifyAbsenceRequest(
    @NotBlank(message = "Justification reason is required")
    @Size(min = 10, max = 500, message = "Justification reason must be between 10 and 500 characters")
    String justificationReason
) {
}
