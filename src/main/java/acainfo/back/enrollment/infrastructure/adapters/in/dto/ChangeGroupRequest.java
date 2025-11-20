package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for changing a student's enrollment to a different group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to change to a different group")
public class ChangeGroupRequest {

    @NotNull(message = "New group ID is required")
    @Schema(description = "ID of the new subject group", example = "2", required = true)
    private Long newGroupId;
}
