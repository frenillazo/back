package acainfo.back.subjectgroup.infrastructure.adapters.in.dto;

import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing subjectGroup.
 * Note: Classrooms are now assigned per schedule, not per subjectGroup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a subjectGroup")
public class UpdateSubjectGroupRequest {

    @Schema(description = "Type of the subjectGroup (REGULAR or INTENSIVO)", example = "REGULAR")
    private GroupType type;

    @Schema(description = "Academic period when the subjectGroup will run", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @Schema(description = "Status of the subjectGroup", example = "ACTIVO")
    private GroupStatus status;

    @Min(value = 1, message = "Max capacity must be at least 1")
    @Schema(description = "Maximum enrollment capacity for the subjectGroup", example = "30")
    private Integer maxCapacity;

    @Schema(description = "ID of the teacher assigned to this subjectGroup", example = "3")
    private Long teacherId;
}
