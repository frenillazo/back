package acainfo.back.session.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a session's mode.
 */
public record UpdateSessionModeRequest(
    @NotNull(message = "New mode is required")
    String mode, // PRESENCIAL, DUAL, ONLINE

    String classroom, // Required if changing to PRESENCIAL or DUAL

    @Size(max = 100, message = "Zoom meeting ID must not exceed 100 characters")
    String zoomMeetingId, // Required if changing to ONLINE or DUAL

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason // Optional reason for the change
) {
}
