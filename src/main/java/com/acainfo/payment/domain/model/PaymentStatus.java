package com.acainfo.payment.domain.model;

/**
 * Status of a payment.
 * Note: OVERDUE is not a status but a calculated state (isPending && now > dueDate).
 */
public enum PaymentStatus {

    /**
     * Payment generated, waiting for payment.
     */
    PENDING,

    /**
     * Payment completed successfully.
     */
    PAID,

    /**
     * Payment cancelled (e.g., student withdrew from enrollment).
     */
    CANCELLED
}
