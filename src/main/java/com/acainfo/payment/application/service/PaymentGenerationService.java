package com.acainfo.payment.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.payment.application.dto.GenerateGroupPaymentsCommand;
import com.acainfo.payment.application.dto.GenerateMonthlyPaymentsCommand;
import com.acainfo.payment.application.dto.GeneratePaymentCommand;
import com.acainfo.payment.application.dto.GroupPaymentPreviewResponse;
import com.acainfo.payment.application.dto.GroupPaymentPreviewResponse.EnrollmentPaymentPreview;
import com.acainfo.payment.application.port.in.GenerateGroupPaymentsUseCase;
import com.acainfo.payment.application.port.in.GenerateMonthlyPaymentsUseCase;
import com.acainfo.payment.application.port.in.GeneratePaymentUseCase;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.exception.PaymentAlreadyExistsException;
import com.acainfo.payment.domain.exception.PaymentCalculationException;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for payment generation operations.
 * Implements GeneratePaymentUseCase and GenerateMonthlyPaymentsUseCase.
 *
 * <p>Payment calculation rules:</p>
 * <ul>
 *   <li>INITIAL: Remaining sessions of current month × hours × pricePerHour</li>
 *   <li>MONTHLY: All sessions of that month × hours × pricePerHour</li>
 *   <li>INTENSIVE_FULL: All sessions of the intensive course × hours × pricePerHour</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentGenerationService implements GeneratePaymentUseCase, GenerateMonthlyPaymentsUseCase, GenerateGroupPaymentsUseCase {

    private static final int PAYMENT_DUE_DAYS = 5;
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");

    private final PaymentRepositoryPort paymentRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;
    private final SessionRepositoryPort sessionRepository;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final GetSubjectUseCase getSubjectUseCase;

    @Override
    public Payment generate(GeneratePaymentCommand command) {
        log.debug("Generating payment for enrollment {} type {} period {}/{}",
                command.enrollmentId(), command.type(), command.billingMonth(), command.billingYear());

        // 1. Get enrollment
        Enrollment enrollment = enrollmentRepository.findById(command.enrollmentId())
                .orElseThrow(() -> new EnrollmentNotFoundException(command.enrollmentId()));

        // 2. Check for duplicate payment
        if (paymentRepository.existsByEnrollmentIdAndBillingPeriod(
                command.enrollmentId(), command.billingMonth(), command.billingYear())) {
            throw new PaymentAlreadyExistsException(
                    command.enrollmentId(), command.billingMonth(), command.billingYear());
        }

        // 3. Get group for pricing and session calculation
        SubjectGroup group = groupRepository.findById(enrollment.getGroupId())
                .orElseThrow(() -> new PaymentCalculationException(
                        "Group not found for enrollment: " + command.enrollmentId()));

        // 4. Calculate payment amount based on type
        PaymentCalculation calculation = calculatePayment(command, enrollment, group);

        // 5. Create and save payment
        LocalDate today = LocalDate.now();
        Payment payment = Payment.builder()
                .enrollmentId(command.enrollmentId())
                .studentId(enrollment.getStudentId())
                .type(command.type())
                .status(PaymentStatus.PENDING)
                .amount(calculation.amount())
                .totalHours(calculation.totalHours())
                .pricePerHour(calculation.pricePerHour())
                .billingMonth(command.billingMonth())
                .billingYear(command.billingYear())
                .generatedAt(today)
                .dueDate(today.plusDays(PAYMENT_DUE_DAYS))
                .description(generateDescription(command.type(), command.billingMonth(), command.billingYear()))
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Generated payment {} for enrollment {} amount {}",
                saved.getId(), command.enrollmentId(), saved.getAmount());

        return saved;
    }

    @Override
    public List<Payment> generateMonthlyPayments(GenerateMonthlyPaymentsCommand command) {
        log.info("Generating monthly payments for period {}/{}",
                command.billingMonth(), command.billingYear());

        // Find all active enrollments in REGULAR groups
        List<Enrollment> activeEnrollments = findActiveRegularEnrollments();
        List<Payment> generatedPayments = new ArrayList<>();

        for (Enrollment enrollment : activeEnrollments) {
            try {
                // Skip if payment already exists for this period
                if (paymentRepository.existsByEnrollmentIdAndBillingPeriod(
                        enrollment.getId(), command.billingMonth(), command.billingYear())) {
                    log.debug("Payment already exists for enrollment {} period {}/{}",
                            enrollment.getId(), command.billingMonth(), command.billingYear());
                    continue;
                }

                // Skip if group ends before billing month
                SubjectGroup group = groupRepository.findById(enrollment.getGroupId()).orElse(null);
                if (group == null || !shouldGeneratePaymentForGroup(group, command.billingMonth(), command.billingYear())) {
                    continue;
                }

                // Generate payment
                GeneratePaymentCommand paymentCommand = new GeneratePaymentCommand(
                        enrollment.getId(),
                        PaymentType.MONTHLY,
                        command.billingMonth(),
                        command.billingYear()
                );

                Payment payment = generate(paymentCommand);
                generatedPayments.add(payment);

            } catch (Exception e) {
                log.error("Failed to generate payment for enrollment {}: {}",
                        enrollment.getId(), e.getMessage());
                // Continue with other enrollments
            }
        }

        log.info("Generated {} monthly payments for period {}/{}",
                generatedPayments.size(), command.billingMonth(), command.billingYear());

        return generatedPayments;
    }

    // ==================== Private Helper Methods ====================

    private PaymentCalculation calculatePayment(
            GeneratePaymentCommand command,
            Enrollment enrollment,
            SubjectGroup group) {

        // TODO: Get pricePerHour from enrollment or group configuration
        // For now, using a placeholder - this should be retrieved from enrollment
        BigDecimal pricePerHour = getPricePerHour(enrollment);

        BigDecimal totalHours;
        switch (command.type()) {
            case INITIAL -> totalHours = calculateInitialHours(group, command.billingMonth(), command.billingYear());
            case MONTHLY -> totalHours = calculateMonthlyHours(group, command.billingMonth(), command.billingYear());
            case INTENSIVE_FULL -> totalHours = calculateIntensiveHours(group);
            default -> throw new PaymentCalculationException("Unknown payment type: " + command.type());
        }

        if (totalHours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentCalculationException(
                    "No sessions found for payment calculation. Enrollment: " + enrollment.getId());
        }

        BigDecimal amount = totalHours.multiply(pricePerHour).setScale(2, RoundingMode.HALF_UP);

        return new PaymentCalculation(amount, totalHours, pricePerHour);
    }

    /**
     * Calculate hours for INITIAL payment (remaining sessions of current month from enrollment date).
     */
    private BigDecimal calculateInitialHours(SubjectGroup group, Integer billingMonth, Integer billingYear) {
        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(billingYear, billingMonth);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // Get sessions from today to end of month
        List<Session> sessions = sessionRepository.findByGroupId(group.getId()).stream()
                .filter(s -> !s.getDate().isBefore(today) && !s.getDate().isAfter(monthEnd))
                .filter(s -> !s.isCancelled())
                .toList();

        return calculateTotalHours(sessions);
    }

    /**
     * Calculate hours for MONTHLY payment (all sessions of the billing month).
     */
    private BigDecimal calculateMonthlyHours(SubjectGroup group, Integer billingMonth, Integer billingYear) {
        YearMonth yearMonth = YearMonth.of(billingYear, billingMonth);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Session> sessions = sessionRepository.findByGroupId(group.getId()).stream()
                .filter(s -> !s.getDate().isBefore(monthStart) && !s.getDate().isAfter(monthEnd))
                .filter(s -> !s.isCancelled())
                .toList();

        return calculateTotalHours(sessions);
    }

    /**
     * Calculate hours for INTENSIVE_FULL payment (all sessions of the intensive course).
     */
    private BigDecimal calculateIntensiveHours(SubjectGroup group) {
        List<Session> sessions = sessionRepository.findByGroupId(group.getId()).stream()
                .filter(s -> !s.isCancelled())
                .toList();

        return calculateTotalHours(sessions);
    }

    private BigDecimal calculateTotalHours(List<Session> sessions) {
        long totalMinutes = sessions.stream()
                .mapToLong(Session::getDurationMinutes)
                .sum();

        return new BigDecimal(totalMinutes)
                .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
    }

    /**
     * Get price per hour for an enrollment.
     */
    private BigDecimal getPricePerHour(Enrollment enrollment) {
        if (enrollment.getPricePerHour() == null) {
            throw new PaymentCalculationException(
                    "Price per hour not set for enrollment: " + enrollment.getId());
        }
        return enrollment.getPricePerHour();
    }

    private List<Enrollment> findActiveRegularEnrollments() {
        // Get all active enrollments
        // Then filter by REGULAR groups
        List<Enrollment> allActive = new ArrayList<>();

        // This would ideally be a single query with join
        // For now, we get all and filter
        groupRepository.findAll().stream()
                .filter(SubjectGroup::isRegular)
                .forEach(group -> {
                    List<Enrollment> groupEnrollments = enrollmentRepository
                            .findByGroupIdAndStatus(group.getId(), EnrollmentStatus.ACTIVE);
                    allActive.addAll(groupEnrollments);
                });

        return allActive;
    }

    private boolean shouldGeneratePaymentForGroup(SubjectGroup group, Integer billingMonth, Integer billingYear) {
        // Check if group has sessions in the billing period
        YearMonth yearMonth = YearMonth.of(billingYear, billingMonth);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        return sessionRepository.findByGroupId(group.getId()).stream()
                .anyMatch(s -> !s.getDate().isBefore(monthStart) && !s.getDate().isAfter(monthEnd));
    }

    private String generateDescription(PaymentType type, Integer month, Integer year) {
        return switch (type) {
            case INITIAL -> String.format("Pago inicial - %02d/%d (días restantes del mes)", month, year);
            case MONTHLY -> String.format("Mensualidad %02d/%d", month, year);
            case INTENSIVE_FULL -> String.format("Curso intensivo completo - %02d/%d", month, year);
        };
    }

    /**
     * Internal record for payment calculation result.
     */
    private record PaymentCalculation(BigDecimal amount, BigDecimal totalHours, BigDecimal pricePerHour) {
    }

    // ==================== GenerateGroupPaymentsUseCase Implementation ====================

    @Override
    @Transactional(readOnly = true)
    public GroupPaymentPreviewResponse preview(Long groupId, Integer billingMonth, Integer billingYear) {
        log.debug("Previewing group payments for group {} period {}/{}", groupId, billingMonth, billingYear);

        // 1. Get group
        SubjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // 2. Get subject for name
        Subject subject = getSubjectUseCase.getById(group.getSubjectId());

        // 3. Get active enrollments
        List<Enrollment> activeEnrollments = enrollmentRepository
                .findByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE);

        // 4. Determine payment type based on group type
        PaymentType paymentType = group.isIntensive() ? PaymentType.INTENSIVE_FULL : PaymentType.MONTHLY;

        // 5. Calculate hours for the period
        BigDecimal totalHours;
        if (paymentType == PaymentType.INTENSIVE_FULL) {
            totalHours = calculateIntensiveHours(group);
        } else {
            totalHours = calculateMonthlyHours(group, billingMonth, billingYear);
        }

        BigDecimal groupPricePerHour = group.getEffectivePricePerHour();
        BigDecimal suggestedAmount = totalHours.multiply(groupPricePerHour).setScale(2, RoundingMode.HALF_UP);

        // 6. Build enrollment previews
        List<EnrollmentPaymentPreview> enrollmentPreviews = activeEnrollments.stream()
                .map(enrollment -> {
                    User student = getUserProfileUseCase.getUserById(enrollment.getStudentId());
                    BigDecimal enrollmentPricePerHour = enrollment.getPricePerHour() != null
                            ? enrollment.getPricePerHour()
                            : groupPricePerHour;
                    BigDecimal individualAmount = totalHours.multiply(enrollmentPricePerHour)
                            .setScale(2, RoundingMode.HALF_UP);

                    return new EnrollmentPaymentPreview(
                            enrollment.getId(),
                            enrollment.getStudentId(),
                            student.getFullName(),
                            student.getEmail(),
                            individualAmount
                    );
                })
                .toList();

        return new GroupPaymentPreviewResponse(
                groupId,
                group.getName(),
                subject.getName(),
                groupPricePerHour,
                totalHours,
                suggestedAmount,
                paymentType,
                billingMonth,
                billingYear,
                enrollmentPreviews
        );
    }

    @Override
    public List<Payment> generate(GenerateGroupPaymentsCommand command) {
        log.info("Generating payments for group {} period {}/{} customAmount={}",
                command.groupId(), command.billingMonth(), command.billingYear(), command.customAmount());

        // 1. Get group
        SubjectGroup group = groupRepository.findById(command.groupId())
                .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        // 2. Get active enrollments
        List<Enrollment> activeEnrollments = enrollmentRepository
                .findByGroupIdAndStatus(command.groupId(), EnrollmentStatus.ACTIVE);

        if (activeEnrollments.isEmpty()) {
            log.info("No active enrollments found for group {}", command.groupId());
            return List.of();
        }

        // 3. Determine payment type
        PaymentType paymentType = group.isIntensive() ? PaymentType.INTENSIVE_FULL : PaymentType.MONTHLY;

        // 4. Calculate hours for the period (used if no custom amount)
        BigDecimal totalHours;
        if (paymentType == PaymentType.INTENSIVE_FULL) {
            totalHours = calculateIntensiveHours(group);
        } else {
            totalHours = calculateMonthlyHours(group, command.billingMonth(), command.billingYear());
        }

        // 5. Generate payment for each enrollment
        List<Payment> generatedPayments = new ArrayList<>();
        BigDecimal groupPricePerHour = group.getEffectivePricePerHour();

        for (Enrollment enrollment : activeEnrollments) {
            try {
                // Skip if payment already exists for this period
                if (paymentRepository.existsByEnrollmentIdAndBillingPeriod(
                        enrollment.getId(), command.billingMonth(), command.billingYear())) {
                    log.debug("Payment already exists for enrollment {} period {}/{}",
                            enrollment.getId(), command.billingMonth(), command.billingYear());
                    continue;
                }

                // Calculate amount: use custom if provided, otherwise calculate
                BigDecimal amount;
                BigDecimal pricePerHour;

                if (command.customAmount() != null) {
                    amount = command.customAmount();
                    pricePerHour = groupPricePerHour; // Store group price for reference
                } else {
                    pricePerHour = enrollment.getPricePerHour() != null
                            ? enrollment.getPricePerHour()
                            : groupPricePerHour;
                    amount = totalHours.multiply(pricePerHour).setScale(2, RoundingMode.HALF_UP);
                }

                // Create payment
                LocalDate today = LocalDate.now();
                Payment payment = Payment.builder()
                        .enrollmentId(enrollment.getId())
                        .studentId(enrollment.getStudentId())
                        .type(paymentType)
                        .status(PaymentStatus.PENDING)
                        .amount(amount)
                        .totalHours(totalHours)
                        .pricePerHour(pricePerHour)
                        .billingMonth(command.billingMonth())
                        .billingYear(command.billingYear())
                        .generatedAt(today)
                        .dueDate(today.plusDays(PAYMENT_DUE_DAYS))
                        .description(generateDescription(paymentType, command.billingMonth(), command.billingYear()))
                        .build();

                Payment saved = paymentRepository.save(payment);
                generatedPayments.add(saved);

                log.debug("Generated payment {} for enrollment {} amount {}",
                        saved.getId(), enrollment.getId(), saved.getAmount());

            } catch (Exception e) {
                log.error("Failed to generate payment for enrollment {}: {}",
                        enrollment.getId(), e.getMessage());
                // Continue with other enrollments
            }
        }

        log.info("Generated {} payments for group {} period {}/{}",
                generatedPayments.size(), command.groupId(), command.billingMonth(), command.billingYear());

        return generatedPayments;
    }
}
