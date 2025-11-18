package acainfo.back.session.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for completing a session.
 */
public record CompleteSessionRequest(
    @NotBlank(message = "Topics covered is required")
    @Size(min = 10, max = 1000, message = "Topics covered must be between 10 and 1000 characters")
    String topicsCovered, // Required - what was taught in the session

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes // Optional additional notes
) {
}
