package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for enrollment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Enrollment details response")
public class EnrollmentResponse {

    @Schema(description = "Enrollment ID", example = "1")
    private Long id;

    @Schema(description = "Student ID", example = "5")
    private Long studentId;

    @Schema(description = "Student full name", example = "Juan Pérez")
    private String studentName;

    @Schema(description = "Student email", example = "juan.perez@example.com")
    private String studentEmail;

    @Schema(description = "Subject group ID", example = "1")
    private Long subjectGroupId;

    @Schema(description = "Subject code", example = "ING101")
    private String subjectCode;

    @Schema(description = "Subject name", example = "Fundamentos de Programación")
    private String subjectName;

    @Schema(description = "Group type", example = "REGULAR")
    private String groupType;

    @Schema(description = "Enrollment status", example = "ACTIVE")
    private EnrollmentStatus status;

    @Schema(description = "Enrollment date", example = "2025-01-15T10:30:00")
    private LocalDateTime enrollmentDate;

    @Schema(description = "Cancellation date (if cancelled)", example = "2025-02-20T14:15:00")
    private LocalDateTime cancellationDate;

    @Schema(description = "Cancellation or suspension reason", example = "Student requested withdrawal")
    private String cancellationReason;

    @Schema(description = "Whether online attendance is allowed", example = "false")
    private Boolean onlineAttendanceAllowed;

    @Schema(description = "Enrollment notes", example = "Student requested flexible attendance")
    private String notes;

    @Schema(description = "Creation timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime updatedAt;
}
