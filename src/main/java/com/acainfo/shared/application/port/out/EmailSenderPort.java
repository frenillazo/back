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
}
