package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import acainfo.back.enrollment.domain.model.GroupRequestDomain;
import acainfo.back.enrollment.domain.model.GroupRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for group request responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Group request response information")
public class GroupRequestResponse {

    @Schema(description = "Request ID", example = "1")
    private Long id;

    @Schema(description = "Subject information")
    private SubjectBasicInfo subject;

    @Schema(description = "Student who created the request")
    private StudentBasicInfo requestedBy;

    @Schema(description = "List of students supporting this request")
    private List<StudentBasicInfo> supporters;

    @Schema(description = "Number of supporters", example = "10")
    private Integer supportersCount;

    @Schema(description = "Minimum supporters required", example = "8")
    private Integer minimumSupporters;

    @Schema(description = "Number of supporters still needed", example = "0")
    private Integer supportersNeeded;

    @Schema(description = "Progress towards minimum supporters (percentage)", example = "125.0")
    private Double supportersProgress;

    @Schema(description = "Whether minimum supporters have been reached", example = "true")
    private Boolean hasMinimumSupporters;

    @Schema(description = "Request status", example = "PENDIENTE")
    private GroupRequestStatus status;

    @Schema(description = "Request creation timestamp")
    private LocalDateTime requestedAt;

    @Schema(description = "Request resolution timestamp (if resolved)")
    private LocalDateTime resolvedAt;

    @Schema(description = "Rejection reason (if rejected)")
    private String rejectionReason;

    @Schema(description = "Additional comments")
    private String comments;

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
     * Nested DTO for basic subject information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectBasicInfo {
        @Schema(description = "Subject ID", example = "1")
        private Long id;

        @Schema(description = "Subject code", example = "ING-101")
        private String code;

        @Schema(description = "Subject name", example = "Cálculo I")
        private String name;

        @Schema(description = "Year", example = "1")
        private Integer year;
    }

    /**
     * Converts a GroupRequest domain to a GroupRequestResponse DTO.
     * @deprecated Use {@link GroupRequestResponseMapper#toResponse(GroupRequestDomain)} instead
     */
    @Deprecated
    public static GroupRequestResponse fromEntity(GroupRequestDomain request) {
        // This method is deprecated and should not be used.
        // Use GroupRequestResponseMapper.toResponse() which properly fetches related entities.
        throw new UnsupportedOperationException(
            "fromEntity() is deprecated. Use GroupRequestResponseMapper.toResponse() instead.");
    }
}
