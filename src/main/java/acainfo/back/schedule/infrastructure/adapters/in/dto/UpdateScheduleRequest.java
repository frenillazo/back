package acainfo.back.schedule.infrastructure.adapters.in.dto;

import acainfo.back.schedule.domain.model.Classroom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request DTO for updating an existing schedule.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a schedule")
public class UpdateScheduleRequest {

    @Schema(description = "Day of the week", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Start time", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "End time", example = "11:00")
    private LocalTime endTime;

    @Schema(description = "Classroom", example = "AULA_1")
    private Classroom classroom;
}
