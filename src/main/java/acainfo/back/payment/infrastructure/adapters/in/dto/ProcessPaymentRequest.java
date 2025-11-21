package acainfo.back.payment.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for processing a payment (confirming payment via Stripe).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process a payment")
public class ProcessPaymentRequest {

    @NotBlank(message = "Stripe payment ID is required")
    @Size(max = 100, message = "Stripe payment ID cannot exceed 100 characters")
    @Schema(description = "Stripe payment intent ID", example = "pi_1234567890abcdef", required = true)
    private String stripePaymentId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Optional processing notes", example = "Payment confirmed via Stripe webhook")
    private String notes;
}
