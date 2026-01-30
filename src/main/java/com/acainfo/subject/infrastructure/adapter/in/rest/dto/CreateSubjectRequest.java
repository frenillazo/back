package com.acainfo.subject.infrastructure.adapter.in.rest.dto;

import com.acainfo.subject.domain.model.Degree;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for creating a subject.
 * Maps to CreateSubjectCommand in application layer.
 */
public record CreateSubjectRequest(

        @NotBlank(message = "Subject code is required")
        @Pattern(regexp = "^[A-Z]{3}\\d{3}$", message = "Code must be 3 uppercase letters followed by 3 digits (e.g., ING101)")
        String code,

        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Degree is required")
        Degree degree,

        @Min(value = 1, message = "Year must be between 1 and 4")
        @Max(value = 4, message = "Year must be between 1 and 4")
        Integer year
) {
}
