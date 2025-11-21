package acainfo.back.shared.infrastructure.adapters.in.dto;

import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollmentResponse;
import acainfo.back.payment.infrastructure.adapters.in.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for student dashboard with consolidated information.
 * This is the main response for the student dashboard, combining data from multiple modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student dashboard with consolidated information")
public class StudentDashboardResponse {

    @Schema(description = "Student profile information")
    private StudentProfileResponse profile;

    @Schema(description = "Active enrollments")
    private List<EnrollmentResponse> activeEnrollments;

    @Schema(description = "Enrollments in waiting queue")
    private List<EnrollmentResponse> waitingEnrollments;

    @Schema(description = "Pending payments")
    private List<PaymentResponse> pendingPayments;

    @Schema(description = "Total amount of pending payments", example = "200.00")
    private BigDecimal totalPendingAmount;

    @Schema(description = "Has overdue payments (>5 days)", example = "false")
    private Boolean hasOverduePayments;

    @Schema(description = "Upcoming sessions (next 7 days)")
    private List<UpcomingSessionDTO> upcomingSessions;

    @Schema(description = "Attendance summary statistics")
    private AttendanceSummaryDTO attendanceSummary;

    @Schema(description = "Active alerts and notifications")
    private List<AlertDTO> alerts;

    @Schema(description = "Count of active enrollments", example = "3")
    private Integer activeEnrollmentsCount;

    @Schema(description = "Count of pending group requests created by student", example = "1")
    private Integer pendingGroupRequestsCount;

    @Schema(description = "Count of new materials available", example = "5")
    private Integer newMaterialsCount;
}
