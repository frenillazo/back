package com.acainfo.student.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Aggregated overview response for student dashboard.
 * Contains summarized data from multiple modules.
 */
public record StudentOverviewResponse(
        // Profile
        Long userId,
        String fullName,
        String email,

        // Academic summary
        List<EnrollmentSummary> activeEnrollments,
        int waitingListCount,
        List<UpcomingSessionSummary> upcomingSessions,

        // Payment summary
        PaymentSummary paymentStatus
) {

    /**
     * Summary of an active enrollment with related entity names.
     */
    public record EnrollmentSummary(
            Long enrollmentId,
            Long groupId,
            String subjectName,
            String subjectCode,
            String groupType,
            String teacherName,
            LocalDateTime enrolledAt
    ) {
    }

    /**
     * Summary of an upcoming session with full details.
     */
    public record UpcomingSessionSummary(
            Long sessionId,
            Long groupId,
            Long enrollmentId,
            String subjectName,
            String subjectCode,
            String groupType,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String classroom,
            String sessionStatus,
            boolean hasReservation
    ) {
    }

    /**
     * Summary of payment status.
     */
    public record PaymentSummary(
            boolean canAccessResources,
            boolean hasOverduePayments,
            int pendingPaymentsCount,
            BigDecimal totalPendingAmount,
            LocalDate nextDueDate
    ) {
    }
}
