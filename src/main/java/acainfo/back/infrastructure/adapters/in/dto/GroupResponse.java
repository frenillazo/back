package acainfo.back.infrastructure.adapters.in.dto;

import acainfo.back.domain.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for group responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Group response information")
public class GroupResponse {

    @Schema(description = "Group ID", example = "1")
    private Long id;

    @Schema(description = "Subject information")
    private SubjectBasicInfo subject;

    @Schema(description = "Teacher information (if assigned)")
    private TeacherBasicInfo teacher;

    @Schema(description = "Type of the group", example = "REGULAR")
    private GroupType type;

    @Schema(description = "Academic period", example = "CUATRIMESTRE_1")
    private AcademicPeriod period;

    @Schema(description = "Status of the group", example = "ACTIVO")
    private GroupStatus status;

    @Schema(description = "Classroom assigned", example = "AULA_1")
    private Classroom classroom;

    @Schema(description = "Maximum capacity", example = "24")
    private Integer maxCapacity;

    @Schema(description = "Current occupancy", example = "18")
    private Integer currentOccupancy;

    @Schema(description = "Available places", example = "6")
    private Integer availablePlaces;

    @Schema(description = "Whether the group has available places", example = "true")
    private Boolean hasAvailablePlaces;

    @Schema(description = "Whether the group is full", example = "false")
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

    /**
     * Converts a Group entity to a GroupResponse DTO.
     */
    public static GroupResponse fromEntity(Group group) {
        GroupResponseBuilder builder = GroupResponse.builder()
                .id(group.getId())
                .type(group.getType())
                .period(group.getPeriod())
                .status(group.getStatus())
                .classroom(group.getClassroom())
                .maxCapacity(group.getMaxCapacity())
                .currentOccupancy(group.getCurrentOccupancy())
                .availablePlaces(group.getAvailablePlaces())
                .hasAvailablePlaces(group.hasAvailablePlaces())
                .isFull(group.isFull())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt());

        // Add subject info
        if (group.getSubject() != null) {
            Subject subject = group.getSubject();
            builder.subject(SubjectBasicInfo.builder()
                    .id(subject.getId())
                    .code(subject.getCode())
                    .name(subject.getName())
                    .year(subject.getYear())
                    .degree(subject.getDegree())
                    .build());
        }

        // Add teacher info (if assigned)
        if (group.getTeacher() != null) {
            User teacher = group.getTeacher();
            builder.teacher(TeacherBasicInfo.builder()
                    .id(teacher.getId())
                    .email(teacher.getEmail())
                    .firstName(teacher.getFirstName())
                    .lastName(teacher.getLastName())
                    .fullName(teacher.getFullName())
                    .build());
        }

        return builder.build();
    }
}
