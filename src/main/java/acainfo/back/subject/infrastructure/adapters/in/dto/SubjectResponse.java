package acainfo.back.subject.infrastructure.adapters.in.dto;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for subject response.
 * Note: Mapping to this DTO should be done via SubjectDtoMapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subject response")
public class SubjectResponse {

    @Schema(description = "Subject ID", example = "1")
    private Long id;

    @Schema(description = "Subject code", example = "ING-101")
    private String code;

    @Schema(description = "Subject name", example = "Cálculo I")
    private String name;

    @Schema(description = "Academic year (1-4)", example = "1")
    private Integer year;

    @Schema(description = "Engineering degree", example = "INDUSTRIAL")
    private Degree degree;

    @Schema(description = "Semester (1 or 2)", example = "1")
    private Integer semester;

    @Schema(description = "Subject status", example = "ACTIVO")
    private SubjectStatus status;

    @Schema(description = "Subject description", example = "Introduction to calculus and mathematical analysis")
    private String description;

    @Schema(description = "Creation timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Full name (code - name)", example = "ING-101 - Cálculo I")
    private String fullName;
}
