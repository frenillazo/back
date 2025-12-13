package com.acainfo.payment.domain.model;

/**
 * Type of payment based on when and why it was generated.
 */
public enum PaymentType {

    /**
     * Initial payment when enrolling in a REGULAR group.
     * Covers remaining sessions of the current month.
     */
    INITIAL,

    /**
     * Monthly recurring payment for REGULAR groups.
     * Generated on the 1st of each month, covers all sessions of that month.
     */
    MONTHLY,

    /**
     * Single full payment for INTENSIVE groups.
     * Covers all sessions of the intensive course.
     */
    INTENSIVE_FULL
}
