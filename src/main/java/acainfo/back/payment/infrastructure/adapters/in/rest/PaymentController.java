package acainfo.back.payment.infrastructure.adapters.in.rest;

import acainfo.back.payment.application.services.PaymentService;
import acainfo.back.payment.domain.model.Payment;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.infrastructure.adapters.in.dto.CreatePaymentRequest;
import acainfo.back.payment.infrastructure.adapters.in.dto.PaymentResponse;
import acainfo.back.payment.infrastructure.adapters.in.dto.ProcessPaymentRequest;
import acainfo.back.payment.infrastructure.adapters.in.dto.RefundPaymentRequest;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for payment management.
 * Provides endpoints for creating, processing, and querying payments.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    // ==================== CREATE PAYMENT ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create a new payment",
        description = "Creates a new payment record for a student (admin only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for student {}", request.getStudentId());

        Payment payment = paymentService.createPayment(
            request.getStudentId(),
            request.getAmount(),
            request.getPaymentType(),
            request.getDueDate(),
            request.getDescription(),
            request.getAcademicPeriod()
        );

        PaymentResponse response = PaymentResponse.fromEntity(payment);

        log.info("Payment created successfully: id={}", payment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== PROCESS PAYMENT ====================

    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
        summary = "Process a payment",
        description = "Marks a payment as paid and records Stripe payment ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or payment already paid"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        log.info("Processing payment {} with Stripe payment ID {}", id, request.getStripePaymentId());

        // Get current user
        User currentUser = userRepository.findByEmail(userDetails.getUsername());

        Payment payment = paymentService.processPayment(id, request.getStripePaymentId(), currentUser);
        PaymentResponse response = PaymentResponse.fromEntity(payment);

        log.info("Payment {} processed successfully", id);
        return ResponseEntity.ok(response);
    }

    // ==================== CANCEL PAYMENT ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Cancel a payment",
        description = "Cancels a pending payment (admin only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel paid payment"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        log.info("Cancelling payment {}", id);

        User currentUser = userRepository.findByEmail(userDetails.getUsername());

        Payment payment = paymentService.cancelPayment(id, reason, currentUser);
        PaymentResponse response = PaymentResponse.fromEntity(payment);

        log.info("Payment {} cancelled successfully", id);
        return ResponseEntity.ok(response);
    }

    // ==================== REFUND PAYMENT ====================

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Refund a payment",
        description = "Processes a refund for a paid payment (admin only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or payment not paid"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Valid @RequestBody RefundPaymentRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        log.info("Refunding payment {}", id);

        User currentUser = userRepository.findByEmail(userDetails.getUsername());

        Payment payment = paymentService.refundPayment(id, request.getReason(), currentUser);
        PaymentResponse response = PaymentResponse.fromEntity(payment);

        log.info("Payment {} refunded successfully", id);
        return ResponseEntity.ok(response);
    }

    // ==================== GET PAYMENT ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(
        summary = "Get payment by ID",
        description = "Retrieves payment details by ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(@Parameter(description = "Payment ID") @PathVariable Long id) {
        log.debug("Retrieving payment {}", id);

        Payment payment = paymentService.getPaymentById(id);
        PaymentResponse response = PaymentResponse.fromEntity(payment);

        return ResponseEntity.ok(response);
    }

    // ==================== GET PAYMENTS BY STUDENT ====================

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    @Operation(
        summary = "Get all payments for a student",
        description = "Retrieves all payments for a specific student. Students can only see their own payments."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {

        log.debug("Retrieving payments for student {}", studentId);

        List<Payment> payments = paymentService.getPaymentsByStudent(studentId);
        List<PaymentResponse> response = payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== GET PENDING PAYMENTS ====================

    @GetMapping("/students/{studentId}/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    @Operation(
        summary = "Get pending payments for a student",
        description = "Retrieves all pending and overdue payments for a student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending payments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PaymentResponse>> getPendingPaymentsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {

        log.debug("Retrieving pending payments for student {}", studentId);

        List<Payment> payments = paymentService.getPendingPaymentsByStudent(studentId);
        List<PaymentResponse> response = payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== CHECK OVERDUE STATUS ====================

    @GetMapping("/students/{studentId}/overdue-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    @Operation(
        summary = "Check if student has overdue payments",
        description = "Returns true if student has blocking overdue payments (>5 days)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Overdue status retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Boolean> hasOverduePayments(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {

        log.debug("Checking overdue status for student {}", studentId);

        boolean hasOverdue = paymentService.hasOverduePayments(studentId);
        return ResponseEntity.ok(hasOverdue);
    }

    // ==================== GET TOTAL PENDING ====================

    @GetMapping("/students/{studentId}/total-pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    @Operation(
        summary = "Get total pending amount for student",
        description = "Calculates the total amount of pending and overdue payments for a student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total pending amount calculated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<BigDecimal> getTotalPendingByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {

        log.debug("Calculating total pending for student {}", studentId);

        BigDecimal total = paymentService.calculateTotalPendingByStudent(studentId);
        return ResponseEntity.ok(total);
    }

    // ==================== ADMIN QUERIES ====================

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
        summary = "Get payments by status",
        description = "Retrieves all payments with a specific status (admin/teacher only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status) {

        log.debug("Retrieving payments with status {}", status);

        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        List<PaymentResponse> response = payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/period/{period}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
        summary = "Get payments by academic period",
        description = "Retrieves all payments for a specific academic period (admin/teacher only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<PaymentResponse>> getPaymentsByAcademicPeriod(
            @Parameter(description = "Academic period", example = "2024-Q1") @PathVariable String period) {

        log.debug("Retrieving payments for period {}", period);

        List<Payment> payments = paymentService.getPaymentsByAcademicPeriod(period);
        List<PaymentResponse> response = payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Calculate revenue between dates",
        description = "Calculates total revenue (paid payments) between two dates (admin only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Revenue calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<BigDecimal> calculateRevenue(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Calculating revenue between {} and {}", startDate, endDate);

        BigDecimal revenue = paymentService.calculateRevenueBetween(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/students-with-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get students with overdue payments",
        description = "Returns list of student IDs with overdue payments (admin only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student IDs retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<Long>> getStudentsWithOverduePayments() {
        log.debug("Retrieving students with overdue payments");

        List<Long> studentIds = paymentService.getStudentsWithOverduePayments();
        return ResponseEntity.ok(studentIds);
    }
}
