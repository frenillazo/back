package com.acainfo.intensive.infrastructure.adapter.in.rest.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request body for {@code POST /api/intensives/{id}/sessions/bulk}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkCreateIntensiveSessionsRequest {

    @NotEmpty(message = "Debe indicar al menos una sesión")
    @Valid
    private List<Entry> entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Entry {

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;

        @NotNull
        private Classroom classroom;
    }
}
