package com.acainfo.user.application.port.in;

/**
 * Use case for requesting a password reset email.
 */
public interface RequestPasswordResetUseCase {

    /**
     * Request a password reset for the given email.
     * Sends a reset email if the user exists and is not blocked.
     *
     * @param email User's email address
     */
    void requestPasswordReset(String email);
}
