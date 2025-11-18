package acainfo.back.subjectgroup.infrastructure.adapters.in.dto;

import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering groups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter criteria for searching groups")
public class SubjectGroupFilterDTO {

    @Schema(description = "Filter by subject ID", example = "1")
    private Long subjectId;

    @Schema(description = "Filter by teacher ID", example = "2")
    private Long teacherId;

    @Schema(description = "Filter by subjectGroup type", example = "REGULAR")
    private GroupType type;

    @Schema(description = "Filter by academic period", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @Schema(description = "Filter by subjectGroup status", example = "ACTIVO")
    private GroupStatus status;

    @Schema(description = "Filter by classroom", example = "AULA_1")
    private Classroom classroom;

    @Schema(description = "Filter groups with available places", example = "true")
    private Boolean hasAvailablePlaces;

    @Schema(description = "Filter by year (from subject)", example = "1")
    private Integer year;
}
