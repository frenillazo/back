package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rejecting a group request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reject a group request")
public class RejectGroupRequestRequest {

    @NotBlank(message = "Rejection reason is required")
    @Schema(description = "Reason for rejecting the request",
            example = "No hay suficiente demanda para crear un nuevo grupo",
            required = true)
    private String reason;
}
