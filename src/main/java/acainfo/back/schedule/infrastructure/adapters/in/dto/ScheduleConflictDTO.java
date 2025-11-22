package acainfo.back.schedule.infrastructure.adapters.in.dto;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for reporting schedule conflicts.
 * Used in validation error responses to provide detailed information about conflicts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule conflict information")
public class ScheduleConflictDTO {

    @Schema(description = "Type of conflict", example = "TEACHER_CONFLICT")
    private ConflictType conflictType;

    @Schema(description = "Day of the week", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Start time of the conflicting period", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "End time of the conflicting period", example = "11:00")
    private LocalTime endTime;

    @Schema(description = "Classroom involved in the conflict", example = "AULA_1")
    private Classroom classroom;

    @Schema(description = "Teacher ID involved in the conflict", example = "5")
    private Long teacherId;

    @Schema(description = "List of conflicting schedules")
    private List<ConflictingScheduleDTO> conflictingSchedules;

    @Schema(description = "Human-readable description of the conflict")
    private String message;

    /**
     * Represents a simplified version of a conflicting schedule.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictingScheduleDTO {

        @Schema(description = "Schedule ID", example = "12")
        private Long scheduleId;

        @Schema(description = "SubjectGroup ID", example = "8")
        private Long groupId;

        @Schema(description = "SubjectGroup display name", example = "Base de Datos - Grupo 1 (CUATRIMESTRE_1)")
        private String groupName;

        @Schema(description = "Day of week", example = "MONDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "Start time", example = "09:00")
        private LocalTime startTime;

        @Schema(description = "End time", example = "11:00")
        private LocalTime endTime;

        @Schema(description = "Classroom", example = "AULA_1")
        private Classroom classroom;

        @Schema(description = "Teacher ID", example = "5")
        private Long teacherId;

        @Schema(description = "Teacher name", example = "Juan García")
        private String teacherName;

        /**
         * Creates a ConflictingScheduleDTO from a ScheduleDomain.
         * Note: This is a simplified version that doesn't include group/teacher names
         * which would require additional repository lookups.
         */
        public static ConflictingScheduleDTO fromDomain(ScheduleDomain schedule) {
            return ConflictingScheduleDTO.builder()
                    .scheduleId(schedule.getId())
                    .groupId(schedule.getSubjectGroupId())
                    .groupName("") // TODO: Fetch via SubjectGroupRepositoryPort if needed
                    .dayOfWeek(schedule.getDayOfWeek())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .classroom(schedule.getClassroom())
                    .teacherId(null) // TODO: Fetch via SubjectGroupRepositoryPort if needed
                    .teacherName(null) // TODO: Fetch via UserRepository if needed
                    .build();
        }
    }

    /**
     * Creates a ScheduleConflictDTO from a list of conflicting schedules.
     */
    public static ScheduleConflictDTO fromSchedules(
            ConflictType conflictType,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Classroom classroom,
            Long teacherId,
            List<ScheduleDomain> conflictingSchedules,
            String message
    ) {
        return ScheduleConflictDTO.builder()
                .conflictType(conflictType)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .classroom(classroom)
                .teacherId(teacherId)
                .conflictingSchedules(
                        conflictingSchedules.stream()
                                .map(ConflictingScheduleDTO::fromDomain)
                                .collect(Collectors.toList())
                )
                .message(message)
                .build();
    }

    /**
     * Enum representing the type of schedule conflict.
     */
    public enum ConflictType {
        TEACHER_CONFLICT,
        CLASSROOM_CONFLICT,
        STUDENT_CONFLICT
    }
}
