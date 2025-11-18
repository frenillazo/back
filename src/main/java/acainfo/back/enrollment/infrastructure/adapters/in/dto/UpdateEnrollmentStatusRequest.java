package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating enrollment status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update enrollment status")
public class UpdateEnrollmentStatusRequest {

    @NotNull(message = "New status is required")
    @Schema(description = "New status for the enrollment",
            example = "SUSPENDED",
            required = true)
    private EnrollmentStatus newStatus;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Schema(description = "Optional reason for status change",
            example = "Payment overdue")
    private String reason;
}
