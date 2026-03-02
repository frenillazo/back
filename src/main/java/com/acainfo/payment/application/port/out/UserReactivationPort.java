package com.acainfo.payment.application.port.out;

/**
 * Output port for checking user reactivation after payment changes.
 * Implemented by the user module's status management service.
 */
public interface UserReactivationPort {

    /**
     * Check if a user should be reactivated after a payment is made.
     * Reactivates if: no overdue payments AND has at least one active enrollment.
     *
     * @param userId the user ID to check
     */
    void checkAndReactivateUser(Long userId);
}
