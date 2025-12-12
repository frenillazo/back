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

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Group ID is required")
    private Long groupId;
}
