package acainfo.back.subject.infrastructure.adapters.in.dto;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering subjects.
 * All fields are optional and can be combined.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter criteria for searching subjects")
public class SubjectFilterDTO {

    @Schema(description = "Filter by degree", example = "INDUSTRIAL")
    private Degree degree;

    @Schema(description = "Filter by year (1-4)", example = "1")
    private Integer year;

    @Schema(description = "Filter by semester (1-2)", example = "1")
    private Integer semester;

    @Schema(description = "Filter by status", example = "ACTIVO")
    private SubjectStatus status;

    @Schema(description = "Search term for code or name", example = "Cálculo")
    private String search;
}
