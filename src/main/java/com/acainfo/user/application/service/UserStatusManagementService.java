package com.acainfo.user.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.shared.application.port.out.EmailSenderPort;
import com.acainfo.user.application.port.in.ActivateUsersUseCase;
import com.acainfo.user.application.port.in.DeactivateUsersUseCase;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.ActivationResult;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.DeactivationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing user status based on enrollments.
 * Handles admin batch activation/deactivation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusManagementService implements DeactivateUsersUseCase, ActivateUsersUseCase {

    private final UserRepositoryPort userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final EmailSenderPort emailSender;



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
     * Batch activate INACTIVE users.
     * Only affects INACTIVE users. BLOCKED and ACTIVE users are skipped.
     *
     * @param userIds list of user IDs to process
     * @return result with counts and any errors
     */
    @Override
    @Transactional
    public ActivationResult activateUsers(List<Long> userIds) {
        log.info("Starting batch activation for {} users", userIds.size());

        int activated = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    errors.add("Usuario no encontrado: " + userId);
                    continue;
                }

                // Skip non-INACTIVE users
                if (user.getStatus() != UserStatus.INACTIVE) {
                    skipped++;
                    if (user.getStatus() == UserStatus.BLOCKED) {
                        errors.add("Usuario " + user.getEmail() + " está bloqueado");
                    } else if (user.getStatus() == UserStatus.ACTIVE) {
                        errors.add("Usuario " + user.getEmail() + " ya está activo");
                    }
                    continue;
                }

                // Activate
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
                activated++;

                // Send notification email
                emailSender.sendAccountReactivatedEmail(user.getEmail(), user.getFirstName());

                log.info("Batch activated user {}", user.getEmail());

            } catch (Exception e) {
                log.error("Error activating user {}: {}", userId, e.getMessage());
                errors.add("Error al procesar usuario " + userId + ": " + e.getMessage());
            }
        }

        log.info("Batch activation completed. Activated: {}, Skipped: {}, Errors: {}",
                activated, skipped, errors.size());

        return new ActivationResult(userIds.size(), activated, skipped, errors);
    }

}
