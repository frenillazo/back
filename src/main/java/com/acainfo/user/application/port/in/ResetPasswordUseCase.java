package com.acainfo.user.application.port.in;

/**
 * Use case for resetting a user's password using a reset token.
 */
public interface ResetPasswordUseCase {

    /**
     * Reset the user's password using the provided reset token.
     *
     * @param token       Password reset token from email link
     * @param newPassword New password to set
     */
    void resetPassword(String token, String newPassword);
}
