package com.acainfo.user.application.port.in;

/**
 * Use case for resending email verification.
 */
public interface ResendVerificationUseCase {

    /**
     * Resend verification email to user.
     *
     * @param email User email address
     */
    void resendVerification(String email);
}
