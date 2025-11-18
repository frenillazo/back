package acainfo.back.session.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for cancelling a session.
 */
public record CancelSessionRequest(
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    String reason // Required reason for cancellation
) {
}
