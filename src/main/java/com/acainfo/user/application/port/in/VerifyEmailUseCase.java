package com.acainfo.user.application.port.in;

/**
 * Use case for verifying user email address.
 */
public interface VerifyEmailUseCase {

    /**
     * Verify user email using the verification token.
     *
     * @param token Verification token from email link
     */
    void verifyEmail(String token);
}
