package acainfo.back.payment.application.services;

import acainfo.back.payment.application.ports.in.ManagePaymentUseCase;
import acainfo.back.payment.application.ports.out.PaymentRepositoryPort;
import acainfo.back.payment.domain.exception.PaymentAlreadyPaidException;
import acainfo.back.payment.domain.exception.PaymentNotFoundException;
import acainfo.back.payment.domain.exception.PaymentProcessingException;
import acainfo.back.payment.domain.model.PaymentDomain;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ManagePaymentUseCase.
 * Handles all payment-related business operations following hexagonal architecture.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagePaymentUseCaseImpl implements ManagePaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final UserRepositoryPort userRepository;
    private final StripeService stripeService;

    private static final int OVERDUE_GRACE_DAYS = 5;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentDomain createPayment(CreatePaymentCommand command) {
        log.info("Creating payment for student {}: type={}, amount={}, dueDate={}",
                command.studentId(), command.paymentType(), command.amount(), command.dueDate());

        // Validate student exists and is actually a student
        UserDomain student = userRepository.findById(command.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + command.studentId()));

        if (!student.isStudent()) {
            throw new IllegalArgumentException("User with id " + command.studentId() + " is not a student");
        }

        // Create payment domain
        PaymentDomain payment = PaymentDomain.builder()
                .studentId(command.studentId())
                .amount(command.amount())
                .paymentType(command.paymentType())
                .status(PaymentStatus.PENDIENTE)
                .dueDate(command.dueDate())
                .description(command.description())
                .academicPeriod(command.academicPeriod())
                .build();

        // Validate business rules
        payment.validate();

        // Generate invoice number
        payment = payment.generateInvoiceNumber();

        // Save payment
        PaymentDomain savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully: id={}, invoiceNumber={}",
                savedPayment.getId(), savedPayment.getInvoiceNumber());

        // TODO: Send notification to student about new payment due

        return savedPayment;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentDomain processPayment(ProcessPaymentCommand command) {
        log.info("Processing payment {}: stripePaymentId={}",
                command.paymentId(), command.stripePaymentId());

        PaymentDomain payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        // Verify payment is not already paid
        if (payment.getStatus() == PaymentStatus.PAGADO) {
            throw new PaymentAlreadyPaidException(command.paymentId());
        }

        // Verify Stripe payment if Stripe is enabled
        if (stripeService.isStripeEnabled()) {
            boolean confirmed = stripeService.confirmPaymentIntent(command.stripePaymentId());
            if (!confirmed) {
                throw new PaymentProcessingException(
                        "Stripe payment confirmation failed for payment " + command.paymentId());
            }
        }

        // Update payment with Stripe ID and mark as paid
        payment = payment.withStripePaymentId(command.stripePaymentId());
        payment = payment.markAsPaid(command.processedByUserId());

        PaymentDomain savedPayment = paymentRepository.save(payment);

        log.info("Payment {} processed successfully", command.paymentId());

        // TODO: Send notification to student confirming payment

        return savedPayment;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentDomain cancelPayment(Long paymentId, Long processedByUserId, String reason) {
        log.info("Cancelling payment {}: reason={}", paymentId, reason);

        PaymentDomain payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Use domain method to cancel (validates state transitions)
        payment = payment.cancel(processedByUserId, reason);

        PaymentDomain savedPayment = paymentRepository.save(payment);

        log.info("Payment {} cancelled successfully", paymentId);

        return savedPayment;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentDomain refundPayment(Long paymentId, Long processedByUserId, String reason) {
        log.info("Refunding payment {}: reason={}", paymentId, reason);

        PaymentDomain payment = paymentRepository.findById(paymentId)
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

        // Use domain method to refund (validates state transitions)
        payment = payment.refund(processedByUserId, reason);

        PaymentDomain savedPayment = paymentRepository.save(payment);

        log.info("Payment {} refunded successfully", paymentId);

        // TODO: Send notification to student about refund

        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDomain getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDomain> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDomain> getOverduePayments(Long studentId) {
        return paymentRepository.findByStudentIdAndStatus(studentId, PaymentStatus.ATRASADO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDomain> getPendingPayments(Long studentId) {
        List<PaymentDomain> pendingPayments = paymentRepository.findByStudentIdAndStatus(
                studentId, PaymentStatus.PENDIENTE);
        List<PaymentDomain> overduePayments = paymentRepository.findByStudentIdAndStatus(
                studentId, PaymentStatus.ATRASADO);

        // Combine both lists
        return java.util.Stream.concat(pendingPayments.stream(), overduePayments.stream())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDomain> getAllOverduePayments() {
        return paymentRepository.findOverduePayments();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void generateMonthlyPayments() {
        log.info("Generating monthly payments for all active students");

        // TODO: Implement logic to generate monthly payments
        // This would typically:
        // 1. Find all students with active enrollments
        // 2. Check if they don't already have a payment for this period
        // 3. Create a MENSUAL payment with default amount
        // 4. Send notifications

        log.warn("generateMonthlyPayments not fully implemented yet");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void checkAndMarkOverduePayments() {
        log.info("Running scheduled job to mark overdue payments");

        List<PaymentDomain> overduePayments = paymentRepository.findOverduePayments();

        int count = 0;
        for (PaymentDomain payment : overduePayments) {
            if (payment.getStatus() == PaymentStatus.PENDIENTE && payment.isOverdue()) {
                PaymentDomain updated = payment.markAsOverdue();
                paymentRepository.save(updated);
                count++;

                // TODO: Send notification to student about overdue payment
            }
        }

        log.info("Marked {} payments as overdue", count);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverduePayments(Long studentId) {
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(OVERDUE_GRACE_DAYS);

        List<PaymentDomain> payments = paymentRepository.findByStudentId(studentId);

        return payments.stream()
                .anyMatch(payment ->
                        (payment.getStatus() == PaymentStatus.PENDIENTE ||
                         payment.getStatus() == PaymentStatus.ATRASADO) &&
                        payment.getDueDate() != null &&
                        payment.getDueDate().isBefore(fiveDaysAgo)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDomain> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDomain> getPaymentsByAcademicPeriod(String period) {
        return paymentRepository.findByAcademicPeriod(period);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueBetween(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.calculateRevenueBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPendingByStudent(Long studentId) {
        return paymentRepository.calculateTotalPendingByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getStudentsWithOverduePayments() {
        return paymentRepository.findStudentIdsWithOverduePayments();
    }
}
