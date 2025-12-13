package com.acainfo.payment.infrastructure.adapter.in.rest;

import com.acainfo.payment.application.port.in.CancelPaymentUseCase;
import com.acainfo.payment.application.port.in.GenerateMonthlyPaymentsUseCase;
import com.acainfo.payment.application.port.in.GeneratePaymentUseCase;
import com.acainfo.payment.application.port.in.GetPaymentUseCase;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.*;
import com.acainfo.payment.infrastructure.adapter.in.rest.mapper.PaymentRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Payment admin operations.
 * Handles payment generation, cancellation, and admin queries.
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final GeneratePaymentUseCase generatePaymentUseCase;
    private final GenerateMonthlyPaymentsUseCase generateMonthlyPaymentsUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final PaymentRestMapper mapper;

    /**
     * Generate a payment for an enrollment.
     */
    @PostMapping("/generate")
    public ResponseEntity<PaymentResponse> generatePayment(
            @Valid @RequestBody GeneratePaymentRequest request
    ) {
        Payment payment = generatePaymentUseCase.generate(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(payment));
    }

    /**
     * Generate monthly payments for all active REGULAR enrollments.
     * Typically called by a scheduled job on the 1st of each month.
     */
    @PostMapping("/generate-monthly")
    public ResponseEntity<List<PaymentResponse>> generateMonthlyPayments(
            @Valid @RequestBody GenerateMonthlyPaymentsRequest request
    ) {
        List<Payment> payments = generateMonthlyPaymentsUseCase.generateMonthlyPayments(
                mapper.toCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseList(payments));
    }

    /**
     * Cancel a payment.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id,
            @RequestBody(required = false) CancelPaymentRequest request
    ) {
        Payment payment = cancelPaymentUseCase.cancel(
                mapper.toCommand(id, request != null ? request : new CancelPaymentRequest(null))
        );
        return ResponseEntity.ok(mapper.toResponse(payment));
    }

    /**
     * Get all overdue payments in the system.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<PaymentResponse>> getAllOverdue() {
        List<Payment> payments = getPaymentUseCase.getAllOverdue();
        return ResponseEntity.ok(mapper.toResponseList(payments));
    }
}
