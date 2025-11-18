package acainfo.back.subjectgroup.infrastructure.adapters.in.dto;

import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new subjectGroup.
 * Note: Classrooms are now assigned per schedule, not per subjectGroup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new subjectGroup")
public class CreateSubjectGroupRequest {

    @NotNull(message = "Subject ID is required")
    @Schema(description = "ID of the subject this subjectGroup belongs to", example = "1")
    private Long subjectId;

    @NotNull(message = "SubjectGroup type is required")
    @Schema(description = "Type of the subjectGroup (REGULAR or INTENSIVO)", example = "REGULAR")
    private GroupType type;

    @NotNull(message = "Academic period is required")
    @Schema(description = "Academic period when the subjectGroup will run", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    @Schema(description = "Maximum enrollment capacity for the subjectGroup", example = "24")
    private Integer maxCapacity;

    @Schema(description = "ID of the teacher assigned to this subjectGroup (optional)", example = "2")
    private Long teacherId;
}
