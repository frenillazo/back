package acainfo.back.schedule.infrastructure.adapters.in.dto;

import acainfo.back.schedule.domain.model.Classroom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for Schedule entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule information")
public class ScheduleResponse {

    @Schema(description = "Schedule ID", example = "1")
    private Long id;

    @Schema(description = "SubjectGroup ID", example = "5")
    private Long groupId;

    @Schema(description = "SubjectGroup display name", example = "Base de Datos - Grupo 1 (CUATRIMESTRE_1)")
    private String groupName;

    @Schema(description = "Subject ID", example = "3")
    private Long subjectId;

    @Schema(description = "Subject name", example = "Base de Datos")
    private String subjectName;

    @Schema(description = "Teacher ID", example = "2")
    private Long teacherId;

    @Schema(description = "Teacher full name", example = "Juan García")
    private String teacherName;

    @Schema(description = "Day of the week", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Localized day name", example = "Lunes")
    private String dayOfWeekLocalized;

    @Schema(description = "Start time", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "End time", example = "11:00")
    private LocalTime endTime;

    @Schema(description = "Duration in minutes", example = "120")
    private Long durationInMinutes;

    @Schema(description = "Classroom", example = "AULA_1")
    private Classroom classroom;

    @Schema(description = "Classroom display name", example = "Aula 1")
    private String classroomDisplayName;

    @Schema(description = "Formatted schedule", example = "Lunes 09:00-11:00 (Aula 1)")
    private String formattedSchedule;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last modification timestamp")
    private LocalDateTime updatedAt;
}
