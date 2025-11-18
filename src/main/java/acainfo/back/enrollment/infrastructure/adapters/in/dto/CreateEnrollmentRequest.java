package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new enrollment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new enrollment")
public class CreateEnrollmentRequest {

    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student to enroll", example = "5", required = true)
    private Long studentId;

    @NotNull(message = "Subject group ID is required")
    @Schema(description = "ID of the subject group to enroll in", example = "1", required = true)
    private Long subjectGroupId;

    @Schema(description = "Optional notes about the enrollment", example = "Student requested flexible attendance")
    private String notes;
}
