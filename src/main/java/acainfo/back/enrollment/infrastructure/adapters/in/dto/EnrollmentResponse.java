package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for enrollment responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Enrollment response information")
public class EnrollmentResponse {

    @Schema(description = "Enrollment ID", example = "1")
    private Long id;

    @Schema(description = "Student information")
    private StudentBasicInfo student;

    @Schema(description = "Subject group information")
    private SubjectGroupBasicInfo subjectGroup;

    @Schema(description = "Enrollment status", example = "ACTIVO")
    private EnrollmentStatus status;

    @Schema(description = "Attendance mode", example = "PRESENCIAL")
    private AttendanceMode attendanceMode;

    @Schema(description = "Enrollment date")
    private LocalDateTime enrollmentDate;

    @Schema(description = "Withdrawal date (if withdrawn)")
    private LocalDateTime withdrawalDate;

    @Schema(description = "Withdrawal reason (if withdrawn)")
    private String withdrawalReason;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Nested DTO for basic student information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentBasicInfo {
        @Schema(description = "Student ID", example = "1")
        private Long id;

        @Schema(description = "Student email", example = "estudiante@acainfo.com")
        private String email;

        @Schema(description = "Student first name", example = "María")
        private String firstName;

        @Schema(description = "Student last name", example = "López")
        private String lastName;

        @Schema(description = "Student full name", example = "María López")
        private String fullName;
    }

    /**
     * Nested DTO for basic subject group information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectGroupBasicInfo {
        @Schema(description = "Group ID", example = "1")
        private Long id;

        @Schema(description = "Subject code", example = "ING-101")
        private String subjectCode;

        @Schema(description = "Subject name", example = "Cálculo I")
        private String subjectName;

        @Schema(description = "Group type", example = "REGULAR")
        private String groupType;

        @Schema(description = "Group status", example = "ACTIVO")
        private String groupStatus;

        @Schema(description = "Current occupancy", example = "18/24")
        private String occupancy;
    }
}
