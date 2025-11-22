package acainfo.back.subjectgroup.infrastructure.adapters.in.dto;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for subjectGroup responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SubjectGroup response information")
public class SubjectGroupResponse {

    @Schema(description = "SubjectGroup ID", example = "1")
    private Long id;

    @Schema(description = "Subject information")
    private SubjectBasicInfo subject;

    @Schema(description = "Teacher information (if assigned)")
    private TeacherBasicInfo teacher;

    @Schema(description = "Type of the subjectGroup", example = "REGULAR")
    private GroupType type;

    @Schema(description = "Academic period", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @Schema(description = "Status of the subjectGroup", example = "ACTIVO")
    private GroupStatus status;

    @Schema(description = "Maximum capacity", example = "24")
    private Integer maxCapacity;

    @Schema(description = "Current occupancy", example = "18")
    private Integer currentOccupancy;

    @Schema(description = "Available places", example = "6")
    private Integer availablePlaces;

    @Schema(description = "Whether the subjectGroup has available places", example = "true")
    private Boolean hasAvailablePlaces;

    @Schema(description = "Whether the subjectGroup is full", example = "false")
    private Boolean isFull;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

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

        @Schema(description = "Degree", example = "INFORMATICA")
        private Degree degree;
    }

    /**
     * Nested DTO for basic teacher information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherBasicInfo {
        @Schema(description = "Teacher ID", example = "2")
        private Long id;

        @Schema(description = "Teacher email", example = "profesor@acainfo.com")
        private String email;

        @Schema(description = "Teacher first name", example = "Juan")
        private String firstName;

        @Schema(description = "Teacher last name", example = "García")
        private String lastName;

        @Schema(description = "Teacher full name", example = "Juan García")
        private String fullName;
    }
}
