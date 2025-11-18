package acainfo.back.session.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for postponing a session.
 */
public record PostponeSessionRequest(
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    String reason, // Required reason for postponement

    // Optional: if provided, creates a new recovery session
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime newScheduledStart,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime newScheduledEnd,

    String newClassroom, // Optional - defaults to original if not provided

    String newZoomMeetingId // Optional - defaults to original if not provided
) {
    /**
     * Checks if this request includes rescheduling information.
     */
    public boolean hasReschedulingInfo() {
        return newScheduledStart != null && newScheduledEnd != null;
    }
}
