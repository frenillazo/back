package acainfo.back.session.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new session.
 */
public record CreateSessionRequest(
    @NotNull(message = "Subject group ID is required")
    Long subjectGroupId,

    @NotNull(message = "Session type is required")
    String type, // REGULAR, RECUPERACION, EXTRA

    @NotNull(message = "Scheduled start is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime scheduledStart,

    @NotNull(message = "Scheduled end is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime scheduledEnd,

    @NotNull(message = "Session mode is required")
    String mode, // PRESENCIAL, DUAL, ONLINE

    String classroom, // AULA_1, AULA_2, VIRTUAL (required for PRESENCIAL/DUAL)

    @Size(max = 100, message = "Zoom meeting ID must not exceed 100 characters")
    String zoomMeetingId, // Required for ONLINE/DUAL

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes,

    Long generatedFromScheduleId, // Optional - for sessions generated from schedule

    Long recoveryForSessionId, // Optional - if this is a recovery session

    Long originalSessionId // Optional - if this is a rescheduled session
) {
}
