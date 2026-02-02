package com.acainfo.user.application.service;

import com.acainfo.security.verification.EmailVerificationService;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for cleaning up unverified user accounts.
 * Removes users who registered but never verified their email after a configurable period.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnverifiedUserCleanupService {

    private final UserRepositoryPort userRepository;
    private final EmailVerificationService emailVerificationService;

    /**
     * Number of days after which unverified users are deleted.
     * Default: 7 days
     */
    @Value("${app.cleanup.unverified-users-days:7}")
    private int unverifiedUsersDays;

    /**
     * Scheduled job that runs every day at 4:00 AM.
     * Deletes users with PENDING_ACTIVATION status created more than X days ago.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.info("Starting scheduled job: cleanupUnverifiedUsers (threshold: {} days)", unverifiedUsersDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(unverifiedUsersDays);
        List<User> unverifiedUsers = userRepository.findByStatusAndCreatedAtBefore(
                UserStatus.PENDING_ACTIVATION, cutoffDate);

        if (unverifiedUsers.isEmpty()) {
            log.info("No unverified users found older than {} days. Job completed.", unverifiedUsersDays);
            return;
        }

        int deletedCount = 0;
        int errorCount = 0;

        for (User user : unverifiedUsers) {
            try {
                // Delete verification tokens first
                emailVerificationService.deleteUserTokens(user.getId());

                // Delete the user
                userRepository.delete(user);
                deletedCount++;

                log.info("Deleted unverified user: {} (created: {})",
                        user.getEmail(), user.getCreatedAt());

            } catch (Exception e) {
                errorCount++;
                log.error("Error deleting unverified user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Cleanup completed. Deleted: {}, Errors: {}", deletedCount, errorCount);
    }

    /**
     * Manual cleanup method for admin use.
     *
     * @return number of users deleted
     */
    @Transactional
    public int cleanupNow() {
        log.info("Manual cleanup triggered for unverified users (threshold: {} days)", unverifiedUsersDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(unverifiedUsersDays);
        List<User> unverifiedUsers = userRepository.findByStatusAndCreatedAtBefore(
                UserStatus.PENDING_ACTIVATION, cutoffDate);

        int deletedCount = 0;

        for (User user : unverifiedUsers) {
            try {
                emailVerificationService.deleteUserTokens(user.getId());
                userRepository.delete(user);
                deletedCount++;
                log.info("Deleted unverified user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error deleting unverified user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        return deletedCount;
    }

    /**
     * Get count of unverified users that would be cleaned up.
     *
     * @return count of users pending cleanup
     */
    public int getUnverifiedUserCount() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(unverifiedUsersDays);
        return userRepository.findByStatusAndCreatedAtBefore(
                UserStatus.PENDING_ACTIVATION, cutoffDate).size();
    }
}
