package com.acainfo.payment.application.service;

import com.acainfo.payment.application.port.in.CheckPaymentStatusUseCase;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for payment access and status checks.
 * Implements CheckPaymentStatusUseCase.
 *
 * <p>Used by other modules to verify student access based on payment status.</p>
 * <p>A student's access is blocked if they have overdue payments
 * (PENDING status with dueDate + 5 days grace period exceeded).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentAccessService implements CheckPaymentStatusUseCase {

    private final PaymentRepositoryPort paymentRepository;

    @Override
    public boolean hasOverduePayments(Long studentId) {
        log.debug("Checking overdue payments for student: {}", studentId);
        return paymentRepository.hasOverduePayments(studentId, LocalDate.now());
    }

    @Override
    public boolean canAccessResources(Long studentId) {
        log.debug("Checking resource access for student: {}", studentId);
        // Student can access resources if they DON'T have overdue payments
        return !hasOverduePayments(studentId);
    }

    @Override
    public boolean isUpToDate(Long studentId) {
        log.debug("Checking if student {} is up to date with payments", studentId);
        // Up to date means no pending payments at all
        long pendingCount = paymentRepository.countPendingByStudentId(studentId);
        return pendingCount == 0;
    }
}
