package acainfo.back.infrastructure.adapters.in.dto;

import acainfo.back.domain.model.AcademicPeriod;
import acainfo.back.domain.model.Classroom;
import acainfo.back.domain.model.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new group")
public class CreateGroupRequest {

    @NotNull(message = "Subject ID is required")
    @Schema(description = "ID of the subject this group belongs to", example = "1")
    private Long subjectId;

    @NotNull(message = "Group type is required")
    @Schema(description = "Type of the group (REGULAR or INTENSIVO)", example = "REGULAR")
    private GroupType type;

    @NotNull(message = "Academic period is required")
    @Schema(description = "Academic period when the group will run", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @NotNull(message = "Classroom is required")
    @Schema(description = "Classroom assigned to the group", example = "AULA_1")
    private Classroom classroom;

    @Schema(description = "ID of the teacher assigned to this group (optional)", example = "2")
    private Long teacherId;
}
