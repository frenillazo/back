package com.acainfo.payment.infrastructure.adapter.in.rest;

import com.acainfo.payment.application.dto.PaymentFilters;
import com.acainfo.payment.application.port.in.CheckPaymentStatusUseCase;
import com.acainfo.payment.application.port.in.GetPaymentUseCase;
import com.acainfo.payment.application.port.in.MarkPaymentPaidUseCase;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.MarkPaymentPaidRequest;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.PaymentResponse;
import com.acainfo.payment.infrastructure.adapter.in.rest.mapper.PaymentRestMapper;
import com.acainfo.shared.application.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Payment operations.
 * Handles payment queries and student-facing operations.
 *
 * All responses are enriched with related entity data (student name, subject name, etc.)
 * to reduce the number of API calls the frontend needs to make.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final GetPaymentUseCase getPaymentUseCase;
    private final MarkPaymentPaidUseCase markPaymentPaidUseCase;
    private final CheckPaymentStatusUseCase checkPaymentStatusUseCase;
    private final PaymentRestMapper mapper;
    private final PaymentResponseEnricher paymentResponseEnricher;

    /**
     * Get payment by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        Payment payment = getPaymentUseCase.getById(id);
        return ResponseEntity.ok(paymentResponseEnricher.enrich(payment));
    }

    /**
     * List payments with filters.
     */
    @GetMapping
    public ResponseEntity<PageResponse<PaymentResponse>> listWithFilters(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long enrollmentId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) Integer billingMonth,
            @RequestParam(required = false) Integer billingYear,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        PaymentFilters filters = new PaymentFilters(
                studentId, enrollmentId, status, type,
                billingMonth, billingYear, isOverdue,
                page, size, sortBy, sortDirection
        );

        PageResponse<Payment> result = getPaymentUseCase.findWithFilters(filters);
        List<PaymentResponse> content = paymentResponseEnricher.enrichList(result.content());

        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.pageNumber(),
                result.totalPages(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last()
        ));
    }

    /**
     * Get payments for a student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentResponse>> getByStudentId(@PathVariable Long studentId) {
        List<Payment> payments = getPaymentUseCase.getByStudentId(studentId);
        return ResponseEntity.ok(paymentResponseEnricher.enrichList(payments));
    }

    /**
     * Get payments for an enrollment.
     */
    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<List<PaymentResponse>> getByEnrollmentId(@PathVariable Long enrollmentId) {
        List<Payment> payments = getPaymentUseCase.getByEnrollmentId(enrollmentId);
        return ResponseEntity.ok(paymentResponseEnricher.enrichList(payments));
    }

    /**
     * Get pending payments for a student.
     */
    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<List<PaymentResponse>> getPendingByStudentId(@PathVariable Long studentId) {
        List<Payment> payments = getPaymentUseCase.getPendingByStudentId(studentId);
        return ResponseEntity.ok(paymentResponseEnricher.enrichList(payments));
    }

    /**
     * Get overdue payments for a student.
     */
    @GetMapping("/student/{studentId}/overdue")
    public ResponseEntity<List<PaymentResponse>> getOverdueByStudentId(@PathVariable Long studentId) {
        List<Payment> payments = getPaymentUseCase.getOverdueByStudentId(studentId);
        return ResponseEntity.ok(paymentResponseEnricher.enrichList(payments));
    }

    /**
     * Check if student can access resources (no overdue payments).
     */
    @GetMapping("/student/{studentId}/access")
    public ResponseEntity<AccessStatusResponse> checkAccessStatus(@PathVariable Long studentId) {
        boolean canAccess = checkPaymentStatusUseCase.canAccessResources(studentId);
        boolean hasOverdue = checkPaymentStatusUseCase.hasOverduePayments(studentId);
        boolean isUpToDate = checkPaymentStatusUseCase.isUpToDate(studentId);

        return ResponseEntity.ok(new AccessStatusResponse(canAccess, hasOverdue, isUpToDate));
    }

    /**
     * Mark payment as paid (typically called after Stripe webhook or manual confirmation).
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponse> markAsPaid(
            @PathVariable Long id,
            @RequestBody(required = false) MarkPaymentPaidRequest request
    ) {
        Payment payment = markPaymentPaidUseCase.markAsPaid(
                mapper.toCommand(id, request != null ? request : new MarkPaymentPaidRequest(null))
        );
        return ResponseEntity.ok(paymentResponseEnricher.enrich(payment));
    }

    /**
     * Response DTO for access status check.
     */
    public record AccessStatusResponse(
            boolean canAccessResources,
            boolean hasOverduePayments,
            boolean isUpToDate
    ) {
    }
}
