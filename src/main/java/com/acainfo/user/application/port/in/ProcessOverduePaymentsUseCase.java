package com.acainfo.user.application.port.in;

/**
 * Use case for processing overdue payments and deactivating affected users.
 */
public interface ProcessOverduePaymentsUseCase {

    /**
     * Process all overdue payments in the system.
     * Deactivates ACTIVE users that have overdue payments.
     */
    void processOverduePayments();
}
