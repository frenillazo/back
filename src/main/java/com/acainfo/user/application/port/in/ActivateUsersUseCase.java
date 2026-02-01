package com.acainfo.user.application.port.in;

import com.acainfo.user.infrastructure.adapter.in.rest.dto.ActivationResult;

import java.util.List;

/**
 * Use case for batch activation of users.
 */
public interface ActivateUsersUseCase {

    /**
     * Activate multiple INACTIVE users.
     * Only INACTIVE users will be activated. BLOCKED and ACTIVE users are skipped.
     *
     * @param userIds list of user IDs to process
     * @return result containing counts and any errors
     */
    ActivationResult activateUsers(List<Long> userIds);
}
