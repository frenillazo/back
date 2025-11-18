package acainfo.back.session.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for starting a session.
 */
public record StartSessionRequest(
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes // Optional notes when starting the session
) {
}
