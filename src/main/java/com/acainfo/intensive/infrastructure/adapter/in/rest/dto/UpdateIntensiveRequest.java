package com.acainfo.intensive.infrastructure.adapter.in.rest.dto;

import com.acainfo.intensive.domain.model.IntensiveStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateIntensiveRequest {

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private IntensiveStatus status;

    @DecimalMin(value = "0.01", message = "pricePerHour must be > 0")
    private BigDecimal pricePerHour;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
