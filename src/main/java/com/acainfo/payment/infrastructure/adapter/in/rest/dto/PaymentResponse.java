package com.acainfo.payment.infrastructure.adapter.in.rest.dto;

import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * REST response DTO for Payment.
 */
public record PaymentResponse(
        Long id,
        Long enrollmentId,
        Long studentId,
        PaymentType type,
        PaymentStatus status,
        BigDecimal amount,
        BigDecimal totalHours,
        BigDecimal pricePerHour,
        Integer billingMonth,
        Integer billingYear,
        LocalDate generatedAt,
        LocalDate dueDate,
        LocalDateTime paidAt,
        String description,
        boolean isOverdue,
        Integer daysOverdue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
