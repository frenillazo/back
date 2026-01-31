package com.acainfo.user.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.shared.application.port.out.EmailSenderPort;
import com.acainfo.user.application.port.in.DeactivateUsersUseCase;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.DeactivationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user status based on payments and enrollments.
 * Handles automatic deactivation/reactivation and batch operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusManagementService implements DeactivateUsersUseCase {

    private final UserRepositoryPort userRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final EmailSenderPort emailSender;

    /**
     * Scheduled job that runs every day at 3:00 AM.
     * Deactivates users with overdue payments (PENDING > 5 days from generation).
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processOverduePayments() {
        log.info("Starting scheduled job: processOverduePayments");

        LocalDate today = LocalDate.now();
        List<Payment> overduePayments = paymentRepository.findAllOverdue(today);

        if (overduePayments.isEmpty()) {
            log.info("No overdue payments found. Job completed.");
            return;
        }

        // Group by student to avoid processing the same student multiple times
        Map<Long, List<Payment>> paymentsByStudent = overduePayments.stream()
                .collect(Collectors.groupingBy(Payment::getStudentId));

        int deactivatedCount = 0;
        int skippedCount = 0;

        for (Map.Entry<Long, List<Payment>> entry : paymentsByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<Payment> studentOverduePayments = entry.getValue();

            try {
                User user = userRepository.findById(studentId).orElse(null);
                if (user == null) {
                    log.warn("User not found for studentId: {}", studentId);
                    continue;
                }

                // Only deactivate ACTIVE users (don't touch BLOCKED or already INACTIVE)
                if (user.getStatus() != UserStatus.ACTIVE) {
                    skippedCount++;
                    continue;
                }

                // Calculate total overdue amount for the email
                String overdueInfo = studentOverduePayments.stream()
                        .map(p -> String.format("%.2f€ (vencido hace %d días)",
                                p.getAmount().doubleValue(),
                                p.getDaysOverdue()))
                        .collect(Collectors.joining(", "));

                user.setStatus(UserStatus.INACTIVE);
                userRepository.save(user);
                deactivatedCount++;

                // Send notification email
                emailSender.sendAccountDeactivatedEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        "Tienes pagos pendientes vencidos: " + overdueInfo
                );

                log.info("Deactivated user {} due to overdue payments", user.getEmail());

            } catch (Exception e) {
                log.error("Error processing user {}: {}", studentId, e.getMessage());
            }
        }

        log.info("Job completed. Deactivated: {}, Skipped: {}", deactivatedCount, skippedCount);
    }

    /**
     * Check if a user should be reactivated after a payment is made.
     * Reactivates if: no overdue payments AND has at least one active enrollment.
     *
     * @param userId the user ID to check
     */
    @Transactional
    public void checkAndReactivateUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for reactivation check: {}", userId);
            return;
        }

        // Only reactivate INACTIVE users (don't touch BLOCKED)
        if (user.getStatus() != UserStatus.INACTIVE) {
            return;
        }

        LocalDate today = LocalDate.now();

        // Check if still has overdue payments
        if (paymentRepository.hasOverduePayments(userId, today)) {
            log.debug("User {} still has overdue payments, not reactivating", userId);
            return;
        }

        // Check if has at least one active enrollment
        List<Enrollment> activeEnrollments = enrollmentRepository.findByStudentIdAndStatus(userId, EnrollmentStatus.ACTIVE);
        if (activeEnrollments.isEmpty()) {
            log.debug("User {} has no active enrollments, not reactivating", userId);
            return;
        }

        // Reactivate the user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Send notification email
        emailSender.sendAccountReactivatedEmail(user.getEmail(), user.getFirstName());

        log.info("Reactivated user {} - payments up to date with active enrollments", user.getEmail());
    }

    /**
     * Batch deactivate users without active enrollments.
     * Only affects ACTIVE users. BLOCKED users are skipped.
     *
     * @param userIds list of user IDs to process
     * @return result with counts and any errors
     */
    @Override
    @Transactional
    public DeactivationResult deactivateUsersWithoutEnrollments(List<Long> userIds) {
        log.info("Starting batch deactivation for {} users", userIds.size());

        int deactivated = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    errors.add("Usuario no encontrado: " + userId);
                    continue;
                }

                // Skip non-ACTIVE users
                if (user.getStatus() != UserStatus.ACTIVE) {
                    skipped++;
                    continue;
                }

                // Check if has active enrollments
                List<Enrollment> activeEnrollments = enrollmentRepository.findByStudentIdAndStatus(userId, EnrollmentStatus.ACTIVE);
                if (!activeEnrollments.isEmpty()) {
                    skipped++;
                    errors.add("Usuario " + user.getEmail() + " tiene inscripciones activas");
                    continue;
                }

                // Deactivate
                user.setStatus(UserStatus.INACTIVE);
                userRepository.save(user);
                deactivated++;

                // Send notification email
                emailSender.sendAccountDeactivatedEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        "No tienes inscripciones activas en ninguna asignatura"
                );

                log.info("Batch deactivated user {} - no active enrollments", user.getEmail());

            } catch (Exception e) {
                log.error("Error deactivating user {}: {}", userId, e.getMessage());
                errors.add("Error al procesar usuario " + userId + ": " + e.getMessage());
            }
        }

        log.info("Batch deactivation completed. Deactivated: {}, Skipped: {}, Errors: {}",
                deactivated, skipped, errors.size());

        return new DeactivationResult(userIds.size(), deactivated, skipped, errors);
    }

    /**
     * Get list of ACTIVE users without any active enrollments.
     * Useful for admin to see candidates for batch deactivation.
     *
     * @return list of users that could be deactivated
     */
    public List<User> findActiveUsersWithoutEnrollments() {
        // This would need a custom query - for now, we'll do it in memory
        // In a real scenario, you'd want to add a proper repository method
        log.warn("findActiveUsersWithoutEnrollments: Consider adding a proper repository query for better performance");

        // Get all active students
        // Note: This is inefficient for large datasets. Add proper query if needed.
        return List.of(); // Placeholder - implement if needed
    }
}
