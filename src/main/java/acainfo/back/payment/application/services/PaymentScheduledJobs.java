package acainfo.back.payment.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled jobs for payment management.
 * Runs automated tasks like checking for overdue payments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduledJobs {

    private final PaymentService paymentService;

    /**
     * Check for overdue payments and mark them as ATRASADO.
     * Runs daily at 9:00 AM.
     *
     * Payments are considered overdue if:
     * - Status is PENDIENTE
     * - Due date is more than 5 days in the past
     */
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9:00 AM
    public void checkOverduePayments() {
        log.info("Starting scheduled job: checkOverduePayments");

        try {
            int markedCount = paymentService.markOverduePayments();

            if (markedCount > 0) {
                log.warn("Marked {} payments as overdue", markedCount);
            } else {
                log.info("No overdue payments found");
            }

        } catch (Exception e) {
            log.error("Error running checkOverduePayments scheduled job", e);
        }
    }

    /**
     * Generate monthly payments for all active students.
     * Runs on the 1st day of each month at 1:00 AM.
     *
     * This creates MENSUAL payment records for all students with active enrollments.
     */
    @Scheduled(cron = "0 0 1 1 * *") // 1st day of month at 1:00 AM
    public void generateMonthlyPayments() {
        log.info("Starting scheduled job: generateMonthlyPayments");

        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();

            int createdCount = paymentService.generateMonthlyPayments(year, month, 1);

            if (createdCount > 0) {
                log.info("Generated {} monthly payments for {}/{}", createdCount, year, month);
            } else {
                log.info("No monthly payments generated for {}/{}", year, month);
            }

        } catch (Exception e) {
            log.error("Error running generateMonthlyPayments scheduled job", e);
        }
    }

    /**
     * Send payment reminders to students with payments due in the next 3 days.
     * Runs daily at 10:00 AM.
     */
    @Scheduled(cron = "0 0 10 * * *") // Every day at 10:00 AM
    public void sendPaymentReminders() {
        log.info("Starting scheduled job: sendPaymentReminders");

        try {
            // TODO: Implement payment reminder logic
            // 1. Find payments with due date in next 3 days
            // 2. Send notification to each student
            // 3. Log reminder sent

            log.info("Payment reminder job completed");

        } catch (Exception e) {
            log.error("Error running sendPaymentReminders scheduled job", e);
        }
    }
}
