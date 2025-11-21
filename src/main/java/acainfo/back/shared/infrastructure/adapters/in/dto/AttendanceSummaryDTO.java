package acainfo.back.shared.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for attendance statistics summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance summary statistics")
public class AttendanceSummaryDTO {

    @Schema(description = "Total sessions scheduled", example = "48")
    private Integer totalSessions;

    @Schema(description = "Sessions attended (PRESENTE)", example = "42")
    private Integer attended;

    @Schema(description = "Sessions missed (AUSENTE)", example = "4")
    private Integer absent;

    @Schema(description = "Sessions with tardiness (TARDANZA)", example = "2")
    private Integer late;

    @Schema(description = "Justified absences (JUSTIFICADO)", example = "1")
    private Integer justified;

    @Schema(description = "Attendance percentage", example = "87.5")
    private Double attendancePercentage;

    @Schema(description = "Is attendance at risk (< 75%)", example = "false")
    private Boolean atRisk;

    public static AttendanceSummaryDTO empty() {
        return AttendanceSummaryDTO.builder()
            .totalSessions(0)
            .attended(0)
            .absent(0)
            .late(0)
            .justified(0)
            .attendancePercentage(0.0)
            .atRisk(false)
            .build();
    }

    public void calculatePercentage() {
        if (totalSessions == null || totalSessions == 0) {
            this.attendancePercentage = 0.0;
            this.atRisk = false;
            return;
        }

        int effectiveAttended = (attended != null ? attended : 0) + (justified != null ? justified : 0);
        this.attendancePercentage = (effectiveAttended * 100.0) / totalSessions;
        this.atRisk = this.attendancePercentage < 75.0;
    }
}
