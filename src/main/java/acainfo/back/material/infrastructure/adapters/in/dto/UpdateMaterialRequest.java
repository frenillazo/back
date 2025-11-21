package acainfo.back.material.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating material metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update material metadata")
public class UpdateMaterialRequest {

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Optional description of the material", example = "Ejercicios de programación en Java")
    private String description;

    @Size(max = 100, message = "Topic must not exceed 100 characters")
    @Schema(description = "Topic or unit classification", example = "Tema 1")
    private String topic;

    @Schema(description = "Whether this material requires payment validation", example = "true")
    private Boolean requiresPayment;
}
