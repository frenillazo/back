package com.acainfo.payment.application.service;

import com.acainfo.payment.application.dto.CancelPaymentCommand;
import com.acainfo.payment.application.dto.MarkPaymentPaidCommand;
import com.acainfo.payment.application.port.in.CancelPaymentUseCase;
import com.acainfo.payment.application.port.in.MarkPaymentPaidUseCase;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.exception.InvalidPaymentStateException;
import com.acainfo.payment.domain.exception.PaymentNotFoundException;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.user.application.service.UserStatusManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for payment status management operations.
 * Implements MarkPaymentPaidUseCase and CancelPaymentUseCase.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only PENDING payments can be marked as paid</li>
 *   <li>Only PENDING payments can be cancelled</li>
 *   <li>Marking as paid sets paidAt timestamp and optionally stores Stripe payment intent ID</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional
public class PaymentStatusService implements MarkPaymentPaidUseCase, CancelPaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final UserStatusManagementService userStatusManagementService;

    public PaymentStatusService(
            PaymentRepositoryPort paymentRepository,
            @Lazy UserStatusManagementService userStatusManagementService) {
        this.paymentRepository = paymentRepository;
        this.userStatusManagementService = userStatusManagementService;
    }

    @Override
    public Payment markAsPaid(MarkPaymentPaidCommand command) {
        log.debug("Marking payment {} as paid", command.paymentId());

        // 1. Find payment
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        // 2. Validate current status
        if (!payment.isPending()) {
            throw new InvalidPaymentStateException(
                    command.paymentId(),
                    payment.getStatus(),
                    PaymentStatus.PENDING,
                    "mark as paid"
            );
        }

        // 3. Update payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(command.paidAt() != null ? command.paidAt() : LocalDateTime.now());

        if (command.stripePaymentIntentId() != null) {
            payment.setStripePaymentIntentId(command.stripePaymentIntentId());
        }

        // 4. Save and return
        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} marked as paid. Stripe intent: {}",
                saved.getId(), saved.getStripePaymentIntentId());

        // 5. Check if user should be reactivated (async - after transaction commits)
        try {
            userStatusManagementService.checkAndReactivateUser(saved.getStudentId());
        } catch (Exception e) {
            log.warn("Failed to check user reactivation for student {}: {}",
                    saved.getStudentId(), e.getMessage());
            // Don't fail the payment marking if reactivation check fails
        }

        return saved;
    }

    @Override
    public Payment cancel(CancelPaymentCommand command) {
        log.debug("Cancelling payment {} reason: {}", command.paymentId(), command.reason());

        // 1. Find payment
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        // 2. Validate current status
        if (!payment.isPending()) {
            throw new InvalidPaymentStateException(
                    command.paymentId(),
                    payment.getStatus(),
                    PaymentStatus.PENDING,
                    "cancel"
            );
        }

        // 3. Update payment
        payment.setStatus(PaymentStatus.CANCELLED);

        if (command.reason() != null && !command.reason().isBlank()) {
            String currentDesc = payment.getDescription() != null ? payment.getDescription() : "";
            payment.setDescription(currentDesc + " [Cancelado: " + command.reason() + "]");
        }

        // 4. Save and return
        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} cancelled. Reason: {}", saved.getId(), command.reason());

        return saved;
    }
}
