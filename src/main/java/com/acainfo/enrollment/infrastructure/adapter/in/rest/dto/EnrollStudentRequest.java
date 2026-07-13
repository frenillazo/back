package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * REST DTO for enrolling a student in a group.
 * Request body for POST /api/enrollments
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollStudentRequest {

    @NotNull(message = "El ID de estudiante es obligatorio")
    private Long studentId;

    @NotNull(message = "El ID de grupo es obligatorio")
    private Long courseId;
}
