package acainfo.back.infrastructure.adapters.in.dto;

import acainfo.back.domain.model.Classroom;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request DTO for creating a new schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new schedule")
public class CreateScheduleRequest {

    @NotNull(message = "Group ID is required")
    @Schema(description = "Group ID", example = "5", required = true)
    private Long groupId;

    @NotNull(message = "Day of week is required")
    @Schema(description = "Day of the week", example = "MONDAY", required = true)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @Schema(description = "Start time", example = "09:00", required = true)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "End time", example = "11:00", required = true)
    private LocalTime endTime;

    @NotNull(message = "Classroom is required")
    @Schema(description = "Classroom", example = "AULA_1", required = true)
    private Classroom classroom;
}
