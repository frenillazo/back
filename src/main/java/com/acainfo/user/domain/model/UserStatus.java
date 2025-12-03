package com.acainfo.user.domain.model;

/**
 * Status of a user account.
 * Domain enum - no framework dependencies.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    PENDING_ACTIVATION
}
