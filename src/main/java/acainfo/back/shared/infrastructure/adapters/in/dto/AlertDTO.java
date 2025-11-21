package acainfo.back.shared.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for student alerts and notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alert or notification for a student")
public class AlertDTO {

    @Schema(description = "Alert type", example = "PAYMENT_DUE")
    private AlertType type;

    @Schema(description = "Alert severity", example = "WARNING")
    private AlertSeverity severity;

    @Schema(description = "Alert message", example = "Pago vence en 3 días")
    private String message;

    @Schema(description = "Related entity ID (optional)", example = "123")
    private Long relatedId;

    @Schema(description = "Alert timestamp")
    private LocalDateTime timestamp;

    public enum AlertType {
        PAYMENT_DUE,
        PAYMENT_OVERDUE,
        SESSION_MODE_CHANGED,
        SESSION_POSTPONED,
        SESSION_CANCELED,
        NEW_MATERIAL,
        GROUP_REQUEST_APPROVED,
        GROUP_REQUEST_REJECTED,
        ENROLLMENT_ACTIVATED,
        WAITING_QUEUE_POSITION,
        ATTENDANCE_LOW,
        GENERAL
    }

    public enum AlertSeverity {
        INFO,
        WARNING,
        ERROR,
        SUCCESS
    }

    public static AlertDTO info(AlertType type, String message) {
        return AlertDTO.builder()
            .type(type)
            .severity(AlertSeverity.INFO)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static AlertDTO warning(AlertType type, String message) {
        return AlertDTO.builder()
            .type(type)
            .severity(AlertSeverity.WARNING)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static AlertDTO error(AlertType type, String message) {
        return AlertDTO.builder()
            .type(type)
            .severity(AlertSeverity.ERROR)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static AlertDTO success(AlertType type, String message) {
        return AlertDTO.builder()
            .type(type)
            .severity(AlertSeverity.SUCCESS)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
