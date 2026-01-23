package com.acainfo.user.application.port.in;

import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;

/**
 * Use case for managing user roles and status.
 * Only ADMIN users can perform these operations.
 */
public interface ManageUserRolesUseCase {

    /**
     * Assign a role to a user.
     *
     * @param userId the user ID
     * @param roleType the role to assign
     * @return the updated user
     * @throws com.acainfo.user.domain.exception.UserNotFoundException if user not found
     * @throws IllegalArgumentException if user already has the role
     */
    User assignRole(Long userId, RoleType roleType);

    /**
     * Revoke a role from a user.
     *
     * @param userId the user ID
     * @param roleType the role to revoke
     * @return the updated user
     * @throws com.acainfo.user.domain.exception.UserNotFoundException if user not found
     * @throws IllegalArgumentException if user doesn't have the role or if trying to remove last role
     */
    User revokeRole(Long userId, RoleType roleType);

    /**
     * Update user status (ACTIVE, BLOCKED, etc.).
     *
     * @param userId the user ID
     * @param status the new status
     * @return the updated user
     * @throws com.acainfo.user.domain.exception.UserNotFoundException if user not found
     */
    User updateStatus(Long userId, UserStatus status);
}
