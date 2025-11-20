package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a group request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new group request")
public class CreateGroupRequestRequest {

    @NotNull(message = "Subject ID is required")
    @Schema(description = "ID of the subject for which to request a new group", example = "1", required = true)
    private Long subjectId;

    @NotNull(message = "Requester ID is required")
    @Schema(description = "ID of the student creating the request", example = "1", required = true)
    private Long requesterId;

    @Schema(description = "Optional comments about the request", example = "Necesitamos un grupo en horario de tarde")
    private String comments;
}
