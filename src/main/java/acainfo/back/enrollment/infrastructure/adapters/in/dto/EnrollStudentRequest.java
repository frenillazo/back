package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for enrolling a student in a subject group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to enroll a student in a group")
public class EnrollStudentRequest {

    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student to enroll", example = "1", required = true)
    private Long studentId;

    @NotNull(message = "Group ID is required")
    @Schema(description = "ID of the subject group", example = "1", required = true)
    private Long groupId;
}
