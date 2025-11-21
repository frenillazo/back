package acainfo.back.payment.application.services;

import acainfo.back.payment.domain.exception.OverduePaymentException;
import acainfo.back.payment.domain.exception.PaymentAlreadyPaidException;
import acainfo.back.payment.domain.exception.PaymentNotFoundException;
import acainfo.back.payment.domain.exception.PaymentProcessingException;
import acainfo.back.payment.domain.model.Payment;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.domain.model.PaymentType;
import acainfo.back.payment.infrastructure.adapters.out.PaymentRepository;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing payments and payment processing.
 * Handles all payment-related business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;

    private static final int OVERDUE_GRACE_DAYS = 5;

    /**
     * Create a new payment for a student.
     *
     * @param studentId Student ID
     * @param amount Payment amount
     * @param paymentType Type of payment
     * @param dueDate Due date
     * @param description Optional description
     * @param academicPeriod Academic period
     * @return Created payment
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Payment createPayment(
        Long studentId,
        BigDecimal amount,
        PaymentType paymentType,
        LocalDate dueDate,
        String description,
        String academicPeriod
    ) {
        log.info("Creating payment for student {}: type={}, amount={}, dueDate={}",
            studentId, paymentType, amount, dueDate);

        // Verify student exists
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        if (!student.isStudent()) {
            throw new IllegalArgumentException("User with id " + studentId + " is not a student");
        }

        // Create payment
        Payment payment = Payment.builder()
            .student(student)
            .amount(amount)
            .paymentType(paymentType)
            .status(PaymentStatus.PENDIENTE)
            .dueDate(dueDate)
            .description(description)
            .academicPeriod(academicPeriod)
            .build();

        // Generate invoice number
        payment.generateInvoiceNumber();

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully: id={}, invoiceNumber={}",
            savedPayment.getId(), savedPayment.getInvoiceNumber());

        // TODO: Send notification to student about new payment due

        return savedPayment;
    }

    /**
     * Process a payment (mark as paid and integrate with Stripe).
     *
     * @param paymentId Payment ID
     * @param stripePaymentId Stripe payment intent ID
     * @param processedBy User processing the payment
     * @return Updated payment
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Payment processPayment(Long paymentId, String stripePaymentId, User processedBy) {
        log.info("Processing payment {}: stripePaymentId={}", paymentId, stripePaymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Verify payment is not already paid
        if (payment.getStatus() == PaymentStatus.PAGADO) {
            throw new PaymentAlreadyPaidException(paymentId);
        }

        // Verify Stripe payment if Stripe is enabled
        if (stripeService.isStripeEnabled()) {
            boolean confirmed = stripeService.confirmPaymentIntent(stripePaymentId);
            if (!confirmed) {
                throw new PaymentProcessingException("Stripe payment confirmation failed for payment " + paymentId);
            }
        }

        // Update payment
        payment.setStripePaymentId(stripePaymentId);
        payment.markAsPaid(processedBy);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment {} processed successfully", paymentId);

        // TODO: Send notification to student confirming payment

        return savedPayment;
    }

    /**
     * Cancel a payment.
     *
     * @param paymentId Payment ID
     * @param reason Cancellation reason
     * @param cancelledBy User cancelling the payment
     * @return Updated payment
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Payment cancelPayment(Long paymentId, String reason, User cancelledBy) {
        log.info("Cancelling payment {}: reason={}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.cancel(cancelledBy, reason);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment {} cancelled successfully", paymentId);

        return savedPayment;
    }

    /**
     * Refund a paid payment.
     *
     * @param paymentId Payment ID
     * @param reason Refund reason
     * @param refundedBy User processing the refund
     * @return Updated payment
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Payment refundPayment(Long paymentId, String reason, User refundedBy) {
        log.info("Refunding payment {}: reason={}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Process refund in Stripe if payment has Stripe ID
        if (payment.getStripePaymentId() != null && stripeService.isStripeEnabled()) {
            String refundId = stripeService.refundPayment(
                payment.getStripePaymentId(),
                payment.getAmount(),
                reason
            );
            log.info("Stripe refund created: {}", refundId);
        }

        payment.refund(refundedBy, reason);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment {} refunded successfully", paymentId);

        // TODO: Send notification to student about refund

        return savedPayment;
    }

    /**
     * Check if a student has any overdue payments that block access.
     *
     * @param studentId Student ID
     * @return true if student has blocking overdue payments
     */
    @Transactional(readOnly = true)
    public boolean hasOverduePayments(Long studentId) {
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(OVERDUE_GRACE_DAYS);
        return paymentRepository.hasBlockingPayments(studentId, fiveDaysAgo);
    }

    /**
     * Validate that a student does not have overdue payments (throw exception if they do).
     *
     * @param studentId Student ID
     * @throws OverduePaymentException if student has overdue payments
     */
    @Transactional(readOnly = true)
    public void validateNoOverduePayments(Long studentId) {
        if (hasOverduePayments(studentId)) {
            long overdueCount = paymentRepository.countOverduePaymentsByStudentId(
                studentId,
                LocalDate.now().minusDays(OVERDUE_GRACE_DAYS)
            );
            throw new OverduePaymentException(studentId, (int) overdueCount);
        }
    }

    /**
     * Get all payments for a student.
     *
     * @param studentId Student ID
     * @return List of payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    /**
     * Get pending and overdue payments for a student.
     *
     * @param studentId Student ID
     * @return List of pending/overdue payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getPendingPaymentsByStudent(Long studentId) {
        return paymentRepository.findPendingPaymentsByStudentId(studentId);
    }

    /**
     * Get a payment by ID.
     *
     * @param paymentId Payment ID
     * @return Payment
     */
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    /**
     * Get all payments with a specific status.
     *
     * @param status Payment status
     * @return List of payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * Get payments by academic period.
     *
     * @param period Academic period
     * @return List of payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByAcademicPeriod(String period) {
        return paymentRepository.findByAcademicPeriod(period);
    }

    /**
     * Get paid payments between two dates (for reporting).
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of paid payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaidPaymentsBetween(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findPaidPaymentsBetween(startDate, endDate);
    }

    /**
     * Calculate total revenue between two dates.
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Total revenue
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueBetween(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.calculateRevenueBetween(startDate, endDate);
    }

    /**
     * Calculate total pending amount for a student.
     *
     * @param studentId Student ID
     * @return Total pending amount
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPendingByStudent(Long studentId) {
        return paymentRepository.calculateTotalPendingByStudentId(studentId);
    }

    /**
     * Get all students with overdue payments (for admin view).
     *
     * @return List of student IDs with overdue payments
     */
    @Transactional(readOnly = true)
    public List<Long> getStudentsWithOverduePayments() {
        return paymentRepository.findStudentIdsWithOverduePayments();
    }

    /**
     * Mark overdue payments as ATRASADO (called by scheduled job).
     * Updates all PENDIENTE payments that are >5 days overdue.
     *
     * @return Number of payments marked as overdue
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int markOverduePayments() {
        log.info("Running scheduled job to mark overdue payments");

        List<Payment> overduePayments = paymentRepository.findPaymentsToMarkAsOverdue();

        int count = 0;
        for (Payment payment : overduePayments) {
            payment.markAsOverdue();
            paymentRepository.save(payment);
            count++;

            // TODO: Send notification to student about overdue payment
        }

        log.info("Marked {} payments as overdue", count);
        return count;
    }

    /**
     * Generate monthly payments for all active students (admin operation).
     * Creates MENSUAL payment for the given month/year.
     *
     * @param year Year
     * @param month Month (1-12)
     * @param dueDay Day of month when payment is due (e.g., 1)
     * @return Number of payments created
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int generateMonthlyPayments(int year, int month, int dueDay) {
        log.info("Generating monthly payments for {}/{}", year, month);

        // TODO: Implement logic to find all active students and create monthly payments
        // This would typically:
        // 1. Find all students with active enrollments
        // 2. Check if they don't already have a payment for this period
        // 3. Create a MENSUAL payment with default amount
        // 4. Send notifications

        log.warn("generateMonthlyPayments not fully implemented yet");
        return 0;
    }
}
