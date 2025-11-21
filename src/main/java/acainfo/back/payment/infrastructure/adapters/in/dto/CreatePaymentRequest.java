package acainfo.back.payment.infrastructure.adapters.in.dto;

import acainfo.back.payment.domain.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new payment")
public class CreatePaymentRequest {

    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student", example = "1", required = true)
    private Long studentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Payment amount in EUR", example = "100.00", required = true)
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Type of payment", example = "MENSUAL", required = true)
    private PaymentType paymentType;

    @NotNull(message = "Due date is required")
    @Schema(description = "Payment due date", example = "2024-02-01", required = true)
    private LocalDate dueDate;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Optional payment description", example = "Cuota mensual febrero 2024")
    private String description;

    @Size(max = 20, message = "Academic period cannot exceed 20 characters")
    @Schema(description = "Academic period", example = "2024-Q1")
    private String academicPeriod;
}
