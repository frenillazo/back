package acainfo.back.infrastructure.adapters.in.dto;

import acainfo.back.domain.model.Degree;
import acainfo.back.domain.model.SubjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new subject")
public class CreateSubjectRequest {

    @NotBlank(message = "Subject code is required")
    @Size(min = 3, max = 20, message = "Subject code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "Subject code must follow pattern: XXX-999 (e.g., ING-101)")
    @Schema(description = "Unique subject code", example = "ING-101", required = true)
    private String code;

    @NotBlank(message = "Subject name is required")
    @Size(min = 3, max = 200, message = "Subject name must be between 3 and 200 characters")
    @Schema(description = "Subject name", example = "Cálculo I", required = true)
    private String name;

    @NotNull(message = "Year is required")
    @Min(value = 1, message = "Year must be between 1 and 4")
    @Max(value = 4, message = "Year must be between 1 and 4")
    @Schema(description = "Academic year (1-4)", example = "1", required = true)
    private Integer year;

    @NotNull(message = "Degree is required")
    @Schema(description = "Engineering degree", example = "INDUSTRIAL", required = true)
    private Degree degree;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    @Schema(description = "Semester (1 or 2)", example = "1", required = true)
    private Integer semester;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Subject description", example = "Introduction to calculus and mathematical analysis")
    private String description;

    @Schema(description = "Subject status (defaults to ACTIVO if not specified)", example = "ACTIVO")
    private SubjectStatus status;
}
