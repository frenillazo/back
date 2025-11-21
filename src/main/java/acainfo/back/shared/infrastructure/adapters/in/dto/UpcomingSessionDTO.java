package acainfo.back.shared.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for upcoming session information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upcoming session information")
public class UpcomingSessionDTO {

    @Schema(description = "Session ID", example = "123")
    private Long sessionId;

    @Schema(description = "Subject name", example = "Cálculo I")
    private String subjectName;

    @Schema(description = "Subject code", example = "ING-101")
    private String subjectCode;

    @Schema(description = "Teacher name", example = "Dr. García")
    private String teacherName;

    @Schema(description = "Session start time")
    private LocalDateTime startTime;

    @Schema(description = "Session end time")
    private LocalDateTime endTime;

    @Schema(description = "Session mode", example = "PRESENCIAL")
    private String mode;

    @Schema(description = "Classroom or location", example = "AULA_1")
    private String location;

    @Schema(description = "Zoom meeting ID (if online/dual)", example = "123-456-789")
    private String zoomMeetingId;

    @Schema(description = "Session topic or notes", example = "Derivadas parciales")
    private String topic;

    @Schema(description = "Minutes until session starts", example = "45")
    private Long minutesUntilStart;

    @Schema(description = "Is session today", example = "true")
    private Boolean isToday;

    @Schema(description = "Is session in next hour", example = "false")
    private Boolean isImminent;
}
