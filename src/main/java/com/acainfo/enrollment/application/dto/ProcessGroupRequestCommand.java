package com.acainfo.enrollment.application.dto;

/**
 * Command DTO for admin processing (approve/reject) a group request.
 */
public record ProcessGroupRequestCommand(
        Long groupRequestId,
        Long adminId,
        String adminResponse
) {
}
