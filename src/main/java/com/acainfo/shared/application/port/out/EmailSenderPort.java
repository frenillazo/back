package com.acainfo.shared.application.port.out;

/**
 * Output port for sending emails.
 * Following hexagonal architecture, this interface defines the contract
 * for email sending that will be implemented by infrastructure adapters.
 */
public interface EmailSenderPort {

    /**
     * Send a verification email to a newly registered user.
     *
     * @param to               recipient email address
     * @param userName         user's first name for personalization
     * @param verificationLink complete URL with verification token
     */
    void sendVerificationEmail(String to, String userName, String verificationLink);

    /**
     * Send a notification email when a user's account is deactivated.
     *
     * @param to       recipient email address
     * @param userName user's first name for personalization
     * @param reason   reason for deactivation (e.g., "pagos pendientes", "sin inscripciones")
     */
    void sendAccountDeactivatedEmail(String to, String userName, String reason);

    /**
     * Send a notification email when a user's account is reactivated.
     *
     * @param to       recipient email address
     * @param userName user's first name for personalization
     */
    void sendAccountReactivatedEmail(String to, String userName);

    /**
     * Send a password reset email with a link to reset the password.
     *
     * @param to        recipient email address
     * @param userName  user's first name for personalization
     * @param resetLink complete URL with reset token
     */
    void sendPasswordResetEmail(String to, String userName, String resetLink);
}
