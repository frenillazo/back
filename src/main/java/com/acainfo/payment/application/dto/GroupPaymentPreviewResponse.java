package com.acainfo.payment.application.dto;

import com.acainfo.payment.domain.model.PaymentType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for previewing group payments before generation.
 * Shows calculated amounts for each enrollment in the group.
 */
public record GroupPaymentPreviewResponse(
        Long groupId,
        String groupName,
        String subjectName,
        BigDecimal pricePerHour,
        BigDecimal totalHours,
        BigDecimal suggestedAmount,
        PaymentType paymentType,
        Integer billingMonth,
        Integer billingYear,
        List<EnrollmentPaymentPreview> enrollments
) {
    /**
     * Preview of payment for a single enrollment.
     */
    public record EnrollmentPaymentPreview(
            Long enrollmentId,
            Long studentId,
            String studentName,
            String studentEmail,
            BigDecimal individualAmount
    ) {
    }
}
