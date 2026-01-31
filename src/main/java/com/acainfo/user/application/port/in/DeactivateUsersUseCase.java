package com.acainfo.user.application.port.in;

import com.acainfo.user.infrastructure.adapter.in.rest.dto.DeactivationResult;

import java.util.List;

/**
 * Use case for batch deactivation of users.
 */
public interface DeactivateUsersUseCase {

    /**
     * Deactivate multiple users that don't have active enrollments.
     * Only ACTIVE users will be deactivated. BLOCKED users are skipped.
     *
     * @param userIds list of user IDs to process
     * @return result containing counts and any errors
     */
    DeactivationResult deactivateUsersWithoutEnrollments(List<Long> userIds);
}
