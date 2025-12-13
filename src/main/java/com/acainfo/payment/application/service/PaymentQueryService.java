package com.acainfo.payment.application.service;

import com.acainfo.payment.application.dto.PaymentFilters;
import com.acainfo.payment.application.port.in.GetPaymentUseCase;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.exception.PaymentNotFoundException;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.shared.application.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for payment query operations.
 * Implements GetPaymentUseCase.
 *
 * <p>Provides read-only operations for payments.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService implements GetPaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;

    @Override
    public Payment getById(Long id) {
        log.debug("Getting payment by ID: {}", id);
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    public PageResponse<Payment> findWithFilters(PaymentFilters filters) {
        log.debug("Finding payments with filters: {}", filters);
        Page<Payment> page = paymentRepository.findWithFilters(filters);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    public List<Payment> getByStudentId(Long studentId) {
        log.debug("Getting payments for student: {}", studentId);
        return paymentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Payment> getByEnrollmentId(Long enrollmentId) {
        log.debug("Getting payments for enrollment: {}", enrollmentId);
        return paymentRepository.findByEnrollmentId(enrollmentId);
    }

    @Override
    public List<Payment> getPendingByStudentId(Long studentId) {
        log.debug("Getting pending payments for student: {}", studentId);
        return paymentRepository.findByStudentIdAndStatus(studentId, PaymentStatus.PENDING);
    }

    @Override
    public List<Payment> getOverdueByStudentId(Long studentId) {
        log.debug("Getting overdue payments for student: {}", studentId);
        return paymentRepository.findOverdueByStudentId(studentId, LocalDate.now());
    }

    @Override
    public List<Payment> getAllOverdue() {
        log.debug("Getting all overdue payments");
        return paymentRepository.findAllOverdue(LocalDate.now());
    }
}
