package acainfo.back.infrastructure.adapters.in.dto;

import acainfo.back.domain.model.AcademicPeriod;
import acainfo.back.domain.model.Classroom;
import acainfo.back.domain.model.GroupStatus;
import acainfo.back.domain.model.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a group")
public class UpdateGroupRequest {

    @Schema(description = "Type of the group (REGULAR or INTENSIVO)", example = "REGULAR")
    private GroupType type;

    @Schema(description = "Academic period when the group will run", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @Schema(description = "Status of the group", example = "ACTIVO")
    private GroupStatus status;

    @Schema(description = "Classroom assigned to the group", example = "AULA_2")
    private Classroom classroom;

    @Schema(description = "ID of the teacher assigned to this group", example = "3")
    private Long teacherId;
}
